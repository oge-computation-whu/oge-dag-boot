package whu.edu.cn.ogedagboot.controller


import geotrellis.raster.{ColorMapOptions, DoubleConstantNoDataCellType, FastMapHistogram, Stitcher, Tile}
import geotrellis.layer.SpatialKey
import geotrellis.raster
import geotrellis.raster.ResampleMethods.{Bilinear, CubicConvolution, NearestNeighbor}
import geotrellis.raster.histogram.Histogram
import geotrellis.raster.matching.HistogramMatching
import geotrellis.raster.render.ColorRamps.HeatmapYellowToRed
import geotrellis.raster.render.{ColorMap, ColorRamp, ColorRamps, Exact, GreaterThanOrEqualTo, LessThan, LessThanOrEqualTo, Png, RGB}
import geotrellis.store.file.FileValueReader
import geotrellis.store.{AttributeStore, LayerId, Reader, ValueNotFoundError, ValueReader}
import geotrellis.vector.Extent
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.{PathVariable, RequestMapping, ResponseBody, RestController}

import javax.servlet.http.HttpSession
import scala.actors.migration.ActWithStash.continue
import scala.collection.mutable.ArrayBuffer
import scala.concurrent._
import scala.math.Fractional.Implicits.infixFractionalOps
import scala.math.Integral.Implicits.infixIntegralOps


/**
 * 渲染栅格影像
 */
@RestController
class RenenderPNGController {


  // 将缺失值转为 NoData
  def rasterFunction(): Tile => Tile = {
    tile: Tile => tile.convert(DoubleConstantNoDataCellType)
  }


  // 预定义色带
  val predefinedColorRamp = Array(
    "HeatmapBlueToYellowToRedSpectrum",
    "HeatmapDarkRedToYellowWhite",
    "HeatmapLightPurpleToDarkPurpleToWhite",
    "HeatmapYellowToRed",
    "ClassificationBoldLandUse",
    "ClassificationMutedTerrain",
    "LightToDarkSunset",
    "BlueToRed",
    "BlueToOrange",
    "GreenToRedOrange",
    "LightToDarkGreen",
    "LightYellowToOrange",
    "Magma",
    "Plasma",
    "Inferno",
    "Viridis",
    "Greyscale")


  // 用于存储第一张瓦片
  var firstTile: Tile = null
  var isFirst = true


  /**
   * // TMS瓦片是一个瓦片一个瓦片加载，每次加载一个瓦片，就会进行一次请求，且瓦片请求顺序随机
   *
   * @param layerId
   * @param zoom
   * @param x
   * @param y
   * @param httpSession
   * @return
   */
  @RequestMapping(value = Array("{layerId}/{zoom}/{x}/{y}.png"), produces = Array(MediaType.IMAGE_PNG_VALUE))
  @ResponseBody
  def renderBean(@PathVariable layerId: String, @PathVariable zoom: Int, @PathVariable x: Int, @PathVariable y: Int, httpSession: HttpSession): Array[Byte] = {






            // 从 session 中获取并解析渲染参数

            // 系统色带类型
            val systemColorRamp = httpSession.getAttribute("systemColorRamp")
            println("systemColorRamp: " + systemColorRamp)


            // 灰度分割阈值，为系统色带中 Greyscale 的传入参数
            val thresholdValue = httpSession.getAttribute("thresholdValue").toString.toInt
            println("thresholdValue: " + thresholdValue)

            // 表示传入的颜色值的表达方式——0:RGBA , 1: 0x16进制
            val colorType = httpSession.getAttribute("colorType")
            println("colorType: " + colorType)


            // 用于存储RGBA型色带颜色值
            val rgbaValues = httpSession.getAttribute("rgbaValues").toString.stripPrefix("[").stripSuffix("]").split("\\],\\[").map(_.stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toInt))
            rgbaValues.map(e => {
              println("rgbaValues: " + e.toList.toString())
            })


            // 用于存储16进制色带颜色值
            val hexValues = httpSession.getAttribute("hexValues").toString.stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toString)
            val hexValuesTransToRGB = hexValues.map(e => {
              var red = java.lang.Integer.parseInt(e.substring(2, 4), 16)
              var green = java.lang.Integer.parseInt(e.substring(4, 6), 16)
              var blue = java.lang.Integer.parseInt(e.substring(6, 8), 16)
              var alpha = java.lang.Integer.parseInt(e.substring(8, 10), 16)
              println(red, green, blue, alpha)
              Array(red, green, blue, alpha)
            })

            // 0：没有输入渐变点个数， 1：输入了渐变点个数
            val gradientPointsSelected = httpSession.getAttribute("gradientPointsSelected")
            println("gradientPointsSelected: " + gradientPointsSelected)


            // 渐变点个数，默认为 100
            val gradientPointsNumber = httpSession.getAttribute("gradientPointsNumber").toString.toInt
            println("gradientPointsNumber: " + gradientPointsNumber)


            // 0:不设置分位数， 1：根据直方图自动计算  2：用户自定义
            val colorQuantileSelected = httpSession.getAttribute("colorQuantileSelected").toString.toInt
            println("colorQuantileSelected: " + colorQuantileSelected)


            // 用户自定义颜色分位数
            val colorQuantile = httpSession.getAttribute("colorQuantile").toString.stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toDouble)
            colorQuantile.foreach(e => {
              println("colorQuantile:" + e)
            })

            // 用户输入的渲染灰度范围
            val grayScaleRange = httpSession.getAttribute("grayScaleRange").toString.stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toDouble)
            grayScaleRange.foreach(e => {
              println("grayScaleRange:" + e)
            })


//    // 以下参数为前端传入
//    val systemColorRamp = "HeatmapBlueToYellowToRedSpectrum" // 系统色带类型
//    val thresholdValue = 100 // 灰度分割阈值，为系统色带中 Greyscale 的传入参数
//    val colorType = 0 // 表示传入的颜色值的表达方式 —— 0:RGBA , 1: 0x16进制
//    val rgbaValues: List[Array[Int]] = List(Array(255, 0, 0, 255), Array(0, 255, 0, 255)) // 用于存储RGBA型色带颜色值
//    val hexValues: Array[String] = "0xFF0000FF, 0x00FF00FF".stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toString)
//    val hexValuesTransToRGB = hexValues.map(e => {
//      var red = java.lang.Integer.parseInt(e.substring(2, 4), 16)
//      var green = java.lang.Integer.parseInt(e.substring(4, 6), 16)
//      var blue = java.lang.Integer.parseInt(e.substring(6, 8), 16)
//      var alpha = java.lang.Integer.parseInt(e.substring(8, 10), 16)
//      Array(red, green, blue, alpha)
//    })// 用于存储16进制色带颜色值
//    val gradientPointsSelected = 0 // 0：没有输入渐变点个数， 1：输入了渐变点个数
//    val gradientPointsNumber = 10 // 渐变点个数
//    val colorQuantileSelected = 1 // 0: 不设置分位数， 1：用户自定义
//    val colorQuantile: Array[Double] = (0 to 100).map(_ * 0.01).toArray // 用户自定义颜色分位数
//    val grayScaleRange: Array[Double] = Array(0, 255) // 用户输入的渲染灰度范围
//    val fallbackColor = 0x00000000  // 用于填充超过范围的颜色
//    val noDataColor = 0x00000000  // 用于填充无数据的颜色


    // 色带, 预设值：HeatmapBlueToYellowToRedSpectrum
    var colorRamp = ColorRamps.HeatmapBlueToYellowToRedSpectrum
    var colorMap: ColorMap = colorRamp.toColorMap(colorQuantile)

    // 将每一个瓦片以 png 格式加载
    var png: Png = null
    // 灰度最大最小值（全局）
    var max: Double = grayScaleRange(1)
    var min: Double = grayScaleRange(0)
    // 灰度最大最小值差值
    var diff: Double = max - min




    // 从layerId中取出渲染参数，该方案弃用
    // val renderArray = layerId.split("_")(1).split("-")
    // val palette = renderArray(0)
    // 前端传入的灰度值范围，弃用该参数
    // val min = renderArray(1).toInt
    // val max = renderArray(2).toInt


    val outputPath = "/home/geocube/oge/on-the-fly"
    val catalogPath = new java.io.File(outputPath).toURI
    // 创建存储区
    val attributeStore: AttributeStore = AttributeStore(catalogPath)
    // 创建valuereader，用来读取每个tile的value
    val valueReader: ValueReader[LayerId] = ValueReader(attributeStore, catalogPath)
    // 将缺失值转为 NoData
    val fn: Tile => Tile = this.rasterFunction()
    val tileOpt: Option[Tile] =
      try {
        val reader = valueReader.reader[SpatialKey, Tile](LayerId(layerId, zoom))
        Some(reader.read(x, y))
      } catch {
        case _: ValueNotFoundError =>
          None
      }




    // TODO 获得第一张瓦片
    for (tile <- tileOpt) yield {
      if (isFirst) {
        firstTile = tile
        isFirst = false
      }
    }
    // TODO 第一张瓦片的灰度直方图信息
    val histogram = firstTile.histogramDouble()
    // TODO 第一张瓦片的灰度最大最小值
    // val findMinMaxDouble = firstTile.findMinMaxDouble




    for (tile <- tileOpt) yield {
      val product: Tile = fn(tile)

      // 色带色域
      colorRamp =
        if (systemColorRamp != "null") {
          if (predefinedColorRamp.contains(systemColorRamp)) {
            systemColorRamp match {
              case "HeatmapBlueToYellowToRedSpectrum" => ColorRamps.HeatmapBlueToYellowToRedSpectrum
              case "HeatmapDarkRedToYellowWhite" => ColorRamps.HeatmapDarkRedToYellowWhite
              case "HeatmapLightPurpleToDarkPurpleToWhite" => ColorRamps.HeatmapLightPurpleToDarkPurpleToWhite
              case "HeatmapYellowToRed" => ColorRamps.HeatmapYellowToRed
              case "ClassificationBoldLandUse" => ColorRamps.ClassificationBoldLandUse
              case "ClassificationMutedTerrain" => ColorRamps.ClassificationMutedTerrain
              case "LightToDarkSunset" => ColorRamps.LightToDarkSunset
              case "BlueToRed" => ColorRamps.BlueToRed
              case "BlueToOrange" => ColorRamps.BlueToOrange
              case "GreenToRedOrange" => ColorRamps.GreenToRedOrange
              case "LightToDarkGreen" => ColorRamps.LightToDarkGreen
              case "LightYellowToOrange" => ColorRamps.LightYellowToOrange
              case "Magma" => ColorRamps.Magma
              case "Plasma" => ColorRamps.Plasma
              case "Inferno" => ColorRamps.Inferno
              case "Viridis" => ColorRamps.Viridis
              case "Greyscale" => ColorRamps.greyscale(thresholdValue)
            }
          }
          else {
            throw new Exception("参数错误")
            colorRamp
            // 输入错误，返回预定义值，抛出异常
          }
        } else if (colorType == 0 || colorType == 1) {
          if (colorType == 0) {
            val rgbaColors = rgbaValues.map(c => geotrellis.raster.render.RGBA(c(0), c(1), c(2), c(3)))
            //ColorRamp(rgbaColors: _*).stops(rgbaColors.length)
            ColorRamp(rgbaColors: _*)
          }
          else {
            val rgbaColors = hexValuesTransToRGB.map(c => geotrellis.raster.render.RGBA(c(0), c(1), c(2), c(3)))
            //ColorRamp(rgbaColors: _*).stops(rgbaColors.length)
            ColorRamp(rgbaColors: _*)
          }
        }
        else {
          throw new Exception("参数错误")
          colorRamp
          // 输入错误，返回预定义值，抛出异常
        }


      // 色带渐变点个数
      if (gradientPointsSelected == 1) {
        colorRamp = colorRamp.stops(gradientPointsNumber)
      }


      // 色带颜色分位数
      colorMap = {
        colorQuantileSelected match {
          case 0 => colorRamp.toColorMap(firstTile.histogram)
          case 1 => colorRamp.toColorMap(colorQuantile.map(_ * diff + min))
        }
      }




      // 栅格影像渲染后的 png
      //      png = {
      //        colorQuantileSelected match {
      //          case 0 => product.renderPng(colorMap)
      //          case _ => product.renderPng(colorMap)
      //        }
      //      }
      png = product.renderPng(colorMap)



      // TODO Test =================================================================

      val test01 = {
        ColorMap(
          scala.Predef.Map(
            (min + diff / 11 * 1).toInt -> 0xD76B27FF,
            (min + diff / 11 * 2).toInt -> 0xE68F2DFF,
            (min + diff / 11 * 3).toInt -> 0xF9B737FF,
            (min + diff / 11 * 4).toInt -> 0xF5CF7DFF,
            (min + diff / 11 * 5).toInt -> 0xF0E7BBFF,
            (min + diff / 11 * 6).toInt -> 0xEDECEAFF,
            (min + diff / 11 * 7).toInt -> 0xC8E1E7FF,
            (min + diff / 11 * 8).toInt -> 0xADD8EAFF,
            (min + diff / 11 * 9).toInt -> 0x7FB8D4FF,
            (min + diff / 11 * 10).toInt -> 0x4EA3C8FF,
            (min + diff / 11 * 11).toInt -> 0x2586ABFF,
            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
          )
        )
      }

      val test02 = {
        ColorMap(
          scala.Predef.Map(
            4.5 -> 0xD76B27FF,
            7.5 -> 0xE68F2DFF,
            11.5 -> 0xF9B737FF,
            15.5 -> 0xF5CF7DFF,
            19.5 -> 0xF0E7BBFF,
            23.5 -> 0xEDECEAFF,
            26.5 -> 0xC8E1E7FF,
            31.5 -> 0xADD8EAFF,
            35.0 -> 0x7FB8D4FF,
            45.0 -> 0x4EA3C8FF
          ),
          ColorMap.Options(
            classBoundaryType = LessThanOrEqualTo,
            noDataColor = 0x00000000, // transparent
            fallbackColor = 0x00000000, // transparent
            strict = false
          )
        )
      }

      //png = product.renderPng(test01)


    }

    png.bytes

  }
}






//        val colorMap = {
//          ColorMap(
//            scala.Predef.Map(
//              max -> geotrellis.raster.render.RGBA(255, 255, 255, 255),
//              min -> geotrellis.raster.render.RGBA(0, 0, 0, 20),
//              -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//            ),
//            ColorMap.Options(
//              // classBoundaryType表示瓦片值与颜色值的对应方向,
//              //  如[-∞, 3.5]表示小于等于3.5，此处可以定义成GreaterThan, GreaterThanOrEqualTo, LessThan, LessThanOrEqualTo, Exact
//              classBoundaryType = Exact,
//              //表示瓦片的值为noData的时候显示的颜色
//              noDataColor = 0x00000000, // transparent
//              //表示不在映射范围内的值显示的颜色
//              fallbackColor = 0x00000000, // transparent
//              // 表示如果瓦片数据值不在定义之内，是报错还是使用 fallbackColor 进行渲染
//              strict = false
//            )
//          )
//        }
