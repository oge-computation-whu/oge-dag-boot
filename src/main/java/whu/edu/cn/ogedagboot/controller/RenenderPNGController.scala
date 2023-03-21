package whu.edu.cn.ogedagboot.controller


import geotrellis.raster.{DoubleConstantNoDataCellType, FastMapHistogram, Tile}
import geotrellis.layer.SpatialKey
import geotrellis.raster
import geotrellis.raster.histogram.Histogram
import geotrellis.raster.matching.HistogramMatching
import geotrellis.raster.render.ColorRamps.HeatmapYellowToRed
import geotrellis.raster.render.{ColorMap, ColorRamp, ColorRamps, Exact, GreaterThanOrEqualTo, Png}
import geotrellis.store.file.FileValueReader
import geotrellis.store.{AttributeStore, LayerId, Reader, ValueNotFoundError, ValueReader}
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.{PathVariable, RequestMapping, ResponseBody, RestController}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent._


@RestController
class RenenderPNGController {

  def rasterFunction(): Tile => Tile = {
    tile: Tile => tile.convert(DoubleConstantNoDataCellType)
  }


  // TODO 预定义色带
  val predefinedColorRamp = Array(
    "HeatmapBlueToYellowToRedSpectrum", "HeatmapDarkRedToYellowWhite", "HeatmapLightPurpleToDarkPurpleToWhite",
    "HeatmapYellowToRed", "ClassificationBoldLandUse", "ClassificationMutedTerrain", "LightToDarkSunset", "BlueToRed",
    "BlueToOrange", "GreenToRedOrange", "LightToDarkGreen", "LightYellowToOrange", "Magma", "Plasma", "Inferno", "Viridis", "Greyscale")


  @RequestMapping(value = Array("{layerId}/{zoom}/{x}/{y}.png"), produces = Array(MediaType.IMAGE_PNG_VALUE))
  @ResponseBody
  def renderBean(@PathVariable layerId: String, @PathVariable zoom: Int, @PathVariable x: Int, @PathVariable y: Int): Array[Byte] = {


    println("zoom: "+zoom)
    // TODO
    //  色带,预设值：HeatmapBlueToYellowToRedSpectrum
    var colorRamp = ColorRamps.HeatmapBlueToYellowToRedSpectrum
    // TODO 以下参数为前端传入
    val systemColorRamp = "HeatmapBlueToYellowToRedSpectrum" // TODO 系统色带类型
    val thresholdValue = 100 // TODO 灰度分割阈值，为系统色带中 Greyscale 的传入参数
    val colorType = 0 // TODO 表示传入的颜色值的表达方式——0:RGBA , 1: 0x16进制
    val rgbaValues: List[Array[Int]] = List(Array(255, 0, 0, 255), Array(0, 255, 0, 255)) // TODO 用于存储RGBA型色带颜色值
    val _0x16Values: List[Int] = List(0xD76B27FF, 0x2586ABFF) // TODO 用于存储16进制色带颜色值
    val gradientPointsSelected = 1 // TODO 0：没有输入渐变点个数， 1：输入了渐变点个数
    val gradientPointsNumber = 100 // TODO 渐变点个数,默认为100
    val colorQuantileSelected = 0 // TODO 0:不设置分位数， 1：根据直方图自动计算  2：用户自定义
    val colorQuantile: Array[Double] = Array(0.6, 0.8, 0.9) // TODO 用户自定义颜色分位数
    var colorMap: ColorMap = colorRamp.toColorMap(colorQuantile)
    var png: Png = null

    val renderArray = layerId.split("_")(1).split("-")
    val palette = renderArray(0)
    val min = renderArray(1).toInt
    val max = renderArray(2).toInt


    val outputPath = "/home/geocube/oge/on-the-fly"
    val catalogPath = new java.io.File(outputPath).toURI
    // TODO 创建存储区
    val attributeStore: AttributeStore = AttributeStore(catalogPath)
    // TODO 创建valuereader，用来读取每个tile的value
    val valueReader: ValueReader[LayerId] = ValueReader(attributeStore, catalogPath)
    val fn: Tile => Tile = this.rasterFunction()
    val tileOpt: Option[Tile] =
      try {
        val reader = valueReader.reader[SpatialKey, Tile](LayerId(layerId, zoom))
        Some(reader.read(x, y))
      } catch {
        case _: ValueNotFoundError =>
          None
      }

    for (tile <- tileOpt) yield {
      val product: Tile = fn(tile)


      // TODO 当前缩放等级下，影像像素灰度的最大最小值
      val Min = tile.findMinMax._1
      val Max = tile.findMinMax._2
      val difference = Max - Min


      // TODO 色带色域
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
            throw new Exception("参数异常")
            colorRamp
            // TODO 输入错误，返回预定义值，抛出异常
          }
        }
        else if (colorType == 0 || colorType == 1) {
          if (colorType == 0) {
            val rgbaColors = rgbaValues.map(c => geotrellis.raster.render.RGBA(c(0), c(1), c(2), c(3)))
            ColorRamp(rgbaColors: _*).stops(rgbaColors.length)
          }
          else {
            ColorRamp(_0x16Values: _*).stops(_0x16Values.length)
          }
        }
        else {
          throw new Exception("参数异常")
          colorRamp
          // TODO 输入错误，返回预定义值，抛出异常
        }

      // TODO 色带渐变点个数
      if (gradientPointsSelected == 1) {
        colorRamp = colorRamp.stops(gradientPointsNumber)
      }


      // TODO 色带颜色分位数
      colorMap = {
        colorQuantileSelected match {
          case 0 => colorMap
          case 1 => colorRamp.toColorMap(tile.histogram)
          case 2 => {
            colorRamp.toColorMap(colorQuantile.map(_*difference+Min))
          }
        }
      }

      colorMap.options

      // TODO 栅格影像渲染后的 png
      png = {
        colorQuantileSelected match {
          case 0 => product.renderPng(colorRamp)
          case _ => product.renderPng(colorMap)
        }
      }


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
//              // TODO classBoundaryType表示瓦片值与颜色值的对应方向,
//              //  如[-∞, 3.5]表示小于等于3.5，此处可以定义成GreaterThan, GreaterThanOrEqualTo, LessThan, LessThanOrEqualTo, Exact
//              classBoundaryType = Exact,
//              //TODO 表示瓦片的值为noData的时候显示的颜色
//              noDataColor = 0x00000000, // transparent
//              //TODO 表示不在映射范围内的值显示的颜色
//              fallbackColor = 0x00000000, // transparent
//              // TODO 表示如果瓦片数据值不在定义之内，是报错还是使用 fallbackColor 进行渲染
//              strict = false
//            )
//          )
//        }