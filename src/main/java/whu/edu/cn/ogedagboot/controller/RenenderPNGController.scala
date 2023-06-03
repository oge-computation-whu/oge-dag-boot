//package whu.edu.cn.ogedagboot.controller
//
//
//import geotrellis.layer.SpatialKey
//import geotrellis.raster.render.{ColorMap, LessThanOrEqualTo, Png}
//import geotrellis.raster.{DoubleConstantNoDataCellType, Tile}
//import geotrellis.store.{AttributeStore, LayerId, ValueNotFoundError, ValueReader}
//import org.springframework.http.MediaType
//import org.springframework.web.bind.annotation.{PathVariable, RequestMapping, ResponseBody, RestController}
//
//import javax.servlet.http.HttpSession
//
//
///**
// * 渲染栅格影像
// */
//@RestController
//class RenenderPNGController {
//
//
//  /**
//   * 将缺失值转为 NoData
//   *
//   * @return
//   */
//  def rasterFunction(): Tile => Tile = {
//    tile: Tile => tile.convert(DoubleConstantNoDataCellType)
//  }
//
//  // TODO 参照ArcGIS Pro 配色
//  val predefinedColorMap = Array(
//    "HeatmapBlueToYellowToRed",
//    "HeatmapGreenToYellowToRed",
//    "GreyToBlack",
//    "YellowToGreen",
//    "HellRed", // 地狱红
//    "EarthTones", // 大地色
//    "Turquoise", //松石绿
//    "WaterDepthRange", // 水深范围
//    "Temperature", // 温度
//    "Precipitation", // 降水
//    "Enamel", // 珐琅
//    "Elevation1", // 高程1
//    "Elevation2", // 高程2
//    "Elevation3" // 高程3
//  )
//
//  /**
//   * TODO TMS瓦片是一个瓦片一个瓦片加载，每次加载一个瓦片，就会进行一次请求，且瓦片请求顺序随机
//   *
//   * @param layerId
//   * @param zoom
//   * @param x
//   * @param y
//   * @param httpSession
//   * @return
//   */
//  @RequestMapping(value = Array("{layerId}/{zoom}/{x}/{y}.png"), produces = Array(MediaType.IMAGE_PNG_VALUE))
//  @ResponseBody
//  def renderBean(@PathVariable layerId: String, @PathVariable zoom: Int, @PathVariable x: Int, @PathVariable y: Int, httpSession: HttpSession): Array[Byte] = {
//    // 从 session 中获取并解析渲染参数
//    // 系统色带类型
//    val systemColorRamp = httpSession.getAttribute("systemColorRamp").toString
//    println("systemColorRamp: " + systemColorRamp)
//
//    // 用户输入的渲染灰度范围
//    val grayScaleMax = httpSession.getAttribute("grayScaleMax").toString.toDouble
//    val grayScaleMin = httpSession.getAttribute("grayScaleMin").toString.toDouble
//
//    val colorType = 1 // 表示传入的颜色值的表达方式 —— 0:RGBA , 1: 0x16进制
//    val rgbaValues: Array[Array[Int]] = Array(Array(255, 0, 0, 255), Array(0, 255, 0, 255)) // 用于存储RGBA型色带颜色值
//    val hexValues: Array[String] = "0xFF0000FF, 0x00FF00FF".stripPrefix("[").stripSuffix("]").split(",").map(_.trim.toString) // 用于存储16进制色带颜色值
//    val hexValuesTransToRGB = hexValues.map(e => {
//      var red = java.lang.Integer.parseInt(e.substring(2, 4), 16)
//      var green = java.lang.Integer.parseInt(e.substring(4, 6), 16)
//      var blue = java.lang.Integer.parseInt(e.substring(6, 8), 16)
//      var alpha = java.lang.Integer.parseInt(e.substring(8, 10), 16)
//      Array(red, green, blue, alpha)
//    })
//
//    var fallbackColor = Array(0, 0, 0, 0) // 用于填充超过范围的颜色
//    val noDataColor = Array(0, 0, 0, 0) // 用于填充无数据的颜色
//
//    // 色带, 预设值：HeatmapBlueToYellowToRed
//    //var colorRamp = ColorRamps.HeatmapBlueToYellowToRed
//    var colorMap: ColorMap = null
//    var png: Png = null
//    // 灰度最大最小值（全局）
//    var max: Double = grayScaleMax
//    var min: Double = grayScaleMin
//    // 灰度最大最小值差值
//    var diff: Double = max - min
//
//    val outputPath = "/home/geocube/oge/on-the-fly"
//    val catalogPath = new java.io.File(outputPath).toURI
//    // 创建存储区
//    val attributeStore: AttributeStore = AttributeStore(catalogPath)
//    // 创建valuereader，用来读取每个tile的value
//    val valueReader: ValueReader[LayerId] = ValueReader(attributeStore, catalogPath)
//    // 将缺失值转为 NoData
//    val fn: Tile => Tile = this.rasterFunction()
//    val tileOpt: Option[Tile] =
//      try {
//        val reader = valueReader.reader[SpatialKey, Tile](LayerId(layerId, zoom))
//        Some(reader.read(x, y))
//      } catch {
//        case _: ValueNotFoundError =>
//          None
//      }
//    for (tile <- tileOpt) yield {
//      val product: Tile = fn(tile)
//      colorMap =
//        if (systemColorRamp != "null") {
//          if (predefinedColorMap.contains(systemColorRamp)) {
//            predefinedColorMap(systemColorRamp, min, diff, fallbackColor, noDataColor)
//          }
//          else {
//            throw new Exception("参数错误")
//            // 输入错误，返回预定义值，抛出异常
//          }
//        } else if (colorType == 0 || colorType == 1) {
//          if (colorType == 0) {
//            customColorRamp(rgbaValues, min, diff, noDataColor, fallbackColor)
//          }
//          else {
//            customColorRamp(hexValuesTransToRGB, min, diff, noDataColor, fallbackColor)
//          }
//        }
//        else {
//          throw new Exception("参数错误")
//        }
//      png = product.renderPng(colorMap)
//    }
//    png.bytes
//
//  }
//
//
//  /**
//   * 渲染栅格影像(预定义色带）
//   *
//   * @param systemColorRamp
//   * @param min
//   * @param diff
//   * @param noDataColor
//   * @param fallbackColor
//   * @return
//   */
//  def predefinedColorMap(systemColorRamp: String, min: Double, diff: Double, noDataColor: Array[Int], fallbackColor: Array[Int]): ColorMap = {
//    systemColorRamp match {
//      case "HeatmapBlueToYellowToRed" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0x5813fcFF,
//            (min + diff / 20 * 2).toInt -> 0x6644fdFF,
//            (min + diff / 20 * 3).toInt -> 0x6a6cfdFF,
//            (min + diff / 20 * 4).toInt -> 0x688effFF,
//            (min + diff / 20 * 5).toInt -> 0x61b0feFF,
//            (min + diff / 20 * 6).toInt -> 0x4bd0feFF,
//            (min + diff / 20 * 7).toInt -> 0x1ef1feFF,
//            (min + diff / 20 * 8).toInt -> 0x4efcecFF,
//            (min + diff / 20 * 9).toInt -> 0x84fcd0FF,
//            (min + diff / 20 * 10).toInt -> 0xa7feb3FF,
//            (min + diff / 20 * 11).toInt -> 0xc0fd95FF,
//            (min + diff / 20 * 12).toInt -> 0xd5fe74FF,
//            (min + diff / 20 * 13).toInt -> 0xe7fd4eFF,
//            (min + diff / 20 * 14).toInt -> 0xf5f627FF,
//            (min + diff / 20 * 15).toInt -> 0xfbdb17FF,
//            (min + diff / 20 * 16).toInt -> 0xfebf1fFF,
//            (min + diff / 20 * 17).toInt -> 0xffa212FF,
//            (min + diff / 20 * 18).toInt -> 0xff8311FF,
//            (min + diff / 20 * 19).toInt -> 0xff5d1aFF,
//            (min + diff / 20 * 20).toInt -> 0xff2b18FF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case "HellRed" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0x000004FF,
//            (min + diff / 20 * 2).toInt -> 0x08061eFF,
//            (min + diff / 20 * 3).toInt -> 0x190c3eFF,
//            (min + diff / 20 * 4).toInt -> 0x2f0a5bFF,
//            (min + diff / 20 * 5).toInt -> 0x470b6aFF,
//            (min + diff / 20 * 6).toInt -> 0x5c126eFF,
//            (min + diff / 20 * 7).toInt -> 0x721a6eFF,
//            (min + diff / 20 * 8).toInt -> 0x87216bFF,
//            (min + diff / 20 * 9).toInt -> 0x9d2965FF,
//            (min + diff / 20 * 10).toInt -> 0xb2325bFF,
//            (min + diff / 20 * 11).toInt -> 0xc53d4fFF,
//            (min + diff / 20 * 12).toInt -> 0xd74b40FF,
//            (min + diff / 20 * 13).toInt -> 0xe65c30FF,
//            (min + diff / 20 * 14).toInt -> 0xf1701fFF,
//            (min + diff / 20 * 15).toInt -> 0xf9870eFF,
//            (min + diff / 20 * 16).toInt -> 0xfda007FF,
//            (min + diff / 20 * 17).toInt -> 0xfcba1eFF,
//            (min + diff / 20 * 18).toInt -> 0xf8d440FF,
//            (min + diff / 20 * 19).toInt -> 0xf2ee70FF,
//            (min + diff / 20 * 20).toInt -> 0xfdffa5FF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case "HeatmapGreenToYellowToRed" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0x38a800FF,
//            (min + diff / 20 * 2).toInt -> 0x48b100FF,
//            (min + diff / 20 * 3).toInt -> 0x58bb00FF,
//            (min + diff / 20 * 4).toInt -> 0x6ac400FF,
//            (min + diff / 20 * 5).toInt -> 0x7ecd00FF,
//            (min + diff / 20 * 6).toInt -> 0x92d600FF,
//            (min + diff / 20 * 7).toInt -> 0xa8df00FF,
//            (min + diff / 20 * 8).toInt -> 0xbfe800FF,
//            (min + diff / 20 * 9).toInt -> 0xd8f100FF,
//            (min + diff / 20 * 10).toInt -> 0xf2fa00FF,
//            (min + diff / 20 * 11).toInt -> 0xfff200FF,
//            (min + diff / 20 * 12).toInt -> 0xffd700FF,
//            (min + diff / 20 * 13).toInt -> 0xffbc00FF,
//            (min + diff / 20 * 14).toInt -> 0xffa100FF,
//            (min + diff / 20 * 15).toInt -> 0xff8600FF,
//            (min + diff / 20 * 16).toInt -> 0xff6b00FF,
//            (min + diff / 20 * 17).toInt -> 0xff5100FF,
//            (min + diff / 20 * 18).toInt -> 0xff3600FF,
//            (min + diff / 20 * 19).toInt -> 0xff1b00FF,
//            (min + diff / 20 * 20).toInt -> 0xff0000FF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case "EarthTones" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0xc68062FF,
//            (min + diff / 20 * 2).toInt -> 0xe5d475FF,
//            (min + diff / 20 * 3).toInt -> 0x9a9157FF,
//            (min + diff / 20 * 4).toInt -> 0xe0ad6cFF,
//            (min + diff / 20 * 5).toInt -> 0x99704fFF,
//            (min + diff / 20 * 6).toInt -> 0xc19a6dFF,
//            (min + diff / 20 * 7).toInt -> 0xc8b66fFF,
//            (min + diff / 20 * 8).toInt -> 0xdfa285FF,
//            (min + diff / 20 * 9).toInt -> 0xb39652FF,
//            (min + diff / 20 * 10).toInt -> 0xe4946aFF,
//            (min + diff / 20 * 11).toInt -> 0xb2a450FF,
//            (min + diff / 20 * 12).toInt -> 0xb68452FF,
//            (min + diff / 20 * 13).toInt -> 0xa86751FF,
//            (min + diff / 20 * 14).toInt -> 0xe3bb84FF,
//            (min + diff / 20 * 15).toInt -> 0x9d8151FF,
//            (min + diff / 20 * 16).toInt -> 0xdca679FF,
//            (min + diff / 20 * 17).toInt -> 0xe1ba67FF,
//            (min + diff / 20 * 18).toInt -> 0xd49961FF,
//            (min + diff / 20 * 19).toInt -> 0xaf7c67FF,
//            (min + diff / 20 * 20).toInt -> 0xc6b95eFF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case "Turquoise" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0x440154FF,
//            (min + diff / 20 * 2).toInt -> 0x481568FF,
//            (min + diff / 20 * 3).toInt -> 0x482677FF,
//            (min + diff / 20 * 4).toInt -> 0x453782FF,
//            (min + diff / 20 * 5).toInt -> 0x404789FF,
//            (min + diff / 20 * 6).toInt -> 0x39568cFF,
//            (min + diff / 20 * 7).toInt -> 0x33648eFF,
//            (min + diff / 20 * 8).toInt -> 0x2d718fFF,
//            (min + diff / 20 * 9).toInt -> 0x287e8fFF,
//            (min + diff / 20 * 10).toInt -> 0x238a8eFF,
//            (min + diff / 20 * 11).toInt -> 0x1f978cFF,
//            (min + diff / 20 * 12).toInt -> 0x20a487FF,
//            (min + diff / 20 * 13).toInt -> 0x2ab080FF,
//            (min + diff / 20 * 14).toInt -> 0x3cbc75FF,
//            (min + diff / 20 * 15).toInt -> 0x56c767FF,
//            (min + diff / 20 * 16).toInt -> 0x74d155FF,
//            (min + diff / 20 * 17).toInt -> 0x95d940FF,
//            (min + diff / 20 * 18).toInt -> 0xb9df29FF,
//            (min + diff / 20 * 19).toInt -> 0xdde419FF,
//            (min + diff / 20 * 20).toInt -> 0xfee825FF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case "WaterDepthRange" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0x8a1d5eFF,
//            (min + diff / 20 * 2).toInt -> 0x954985FF,
//            (min + diff / 20 * 3).toInt -> 0x9a75aeFF,
//            (min + diff / 20 * 4).toInt -> 0x989ddaFF,
//            (min + diff / 20 * 5).toInt -> 0x90c2fcFF,
//            (min + diff / 20 * 6).toInt -> 0x8dd2f6FF,
//            (min + diff / 20 * 7).toInt -> 0x87e3f0FF,
//            (min + diff / 20 * 8).toInt -> 0x7ff3e9FF,
//            (min + diff / 20 * 9).toInt -> 0x75fbd3FF,
//            (min + diff / 20 * 10).toInt -> 0x66f6a6FF,
//            (min + diff / 20 * 11).toInt -> 0x4df078FF,
//            (min + diff / 20 * 12).toInt -> 0x2deb40FF,
//            (min + diff / 20 * 13).toInt -> 0x59ed31FF,
//            (min + diff / 20 * 14).toInt -> 0x95f454FF,
//            (min + diff / 20 * 15).toInt -> 0xc3f96eFF,
//            (min + diff / 20 * 16).toInt -> 0xedfe85FF,
//            (min + diff / 20 * 17).toInt -> 0xfad859FF,
//            (min + diff / 20 * 18).toInt -> 0xf39b3aFF,
//            (min + diff / 20 * 19).toInt -> 0xec531cFF,
//            (min + diff / 20 * 20).toInt -> 0xe60000FF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case "Temperature" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0xfdfdfeFF,
//            (min + diff / 20 * 2).toInt -> 0xffc0feFF,
//            (min + diff / 20 * 3).toInt -> 0xff7bfeFF,
//            (min + diff / 20 * 4).toInt -> 0xfd21fcFF,
//            (min + diff / 20 * 5).toInt -> 0xc318fdFF,
//            (min + diff / 20 * 6).toInt -> 0x8116fdFF,
//            (min + diff / 20 * 7).toInt -> 0x6953fdFF,
//            (min + diff / 20 * 8).toInt -> 0x64a5ffFF,
//            (min + diff / 20 * 9).toInt -> 0x1ef1feFF,
//            (min + diff / 20 * 10).toInt -> 0x09fcc4FF,
//            (min + diff / 20 * 11).toInt -> 0x00fd7bFF,
//            (min + diff / 20 * 12).toInt -> 0x00fc22FF,
//            (min + diff / 20 * 13).toInt -> 0x7dfe27FF,
//            (min + diff / 20 * 14).toInt -> 0xcdfe29FF,
//            (min + diff / 20 * 15).toInt -> 0xf6ef25FF,
//            (min + diff / 20 * 16).toInt -> 0xfac922FF,
//            (min + diff / 20 * 17).toInt -> 0xfca114FF,
//            (min + diff / 20 * 18).toInt -> 0xe1751dFF,
//            (min + diff / 20 * 19).toInt -> 0xba4814FF,
//            (min + diff / 20 * 20).toInt -> 0x931d08FF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case "Precipitation" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0xc2523cFF,
//            (min + diff / 20 * 2).toInt -> 0xcd6033FF,
//            (min + diff / 20 * 3).toInt -> 0xd97329FF,
//            (min + diff / 20 * 4).toInt -> 0xe48a1dFF,
//            (min + diff / 20 * 5).toInt -> 0xeea512FF,
//            (min + diff / 20 * 6).toInt -> 0xf3bc0dFF,
//            (min + diff / 20 * 7).toInt -> 0xf7d408FF,
//            (min + diff / 20 * 8).toInt -> 0xfcee03FF,
//            (min + diff / 20 * 9).toInt -> 0xe1fb00FF,
//            (min + diff / 20 * 10).toInt -> 0x99f200FF,
//            (min + diff / 20 * 11).toInt -> 0x56e800FF,
//            (min + diff / 20 * 12).toInt -> 0x17df00FF,
//            (min + diff / 20 * 13).toInt -> 0x07d124FF,
//            (min + diff / 20 * 14).toInt -> 0x11bf54FF,
//            (min + diff / 20 * 15).toInt -> 0x19ae76FF,
//            (min + diff / 20 * 16).toInt -> 0x1f9c8cFF,
//            (min + diff / 20 * 17).toInt -> 0x1b8993FF,
//            (min + diff / 20 * 18).toInt -> 0x15698bFF,
//            (min + diff / 20 * 19).toInt -> 0x104a82FF,
//            (min + diff / 20 * 20).toInt -> 0x0b2c7aFF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case "Enamel" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0x5034c9FF,
//            (min + diff / 20 * 2).toInt -> 0x39ab6dFF,
//            (min + diff / 20 * 3).toInt -> 0xb52729FF,
//            (min + diff / 20 * 4).toInt -> 0x4278a9FF,
//            (min + diff / 20 * 5).toInt -> 0xcbc629FF,
//            (min + diff / 20 * 6).toInt -> 0xb0448aFF,
//            (min + diff / 20 * 7).toInt -> 0x41c92dFF,
//            (min + diff / 20 * 8).toInt -> 0xa3742aFF,
//            (min + diff / 20 * 9).toInt -> 0x4acacaFF,
//            (min + diff / 20 * 10).toInt -> 0xa42ab7FF,
//            (min + diff / 20 * 11).toInt -> 0x3951aeFF,
//            (min + diff / 20 * 12).toInt -> 0x7ca439FF,
//            (min + diff / 20 * 13).toInt -> 0xc44766FF,
//            (min + diff / 20 * 14).toInt -> 0xa2593dFF,
//            (min + diff / 20 * 15).toInt -> 0x97c935FF,
//            (min + diff / 20 * 16).toInt -> 0x39ba55FF,
//            (min + diff / 20 * 17).toInt -> 0x9a9222FF,
//            (min + diff / 20 * 18).toInt -> 0x2aa3c1FF,
//            (min + diff / 20 * 19).toInt -> 0x963e9fFF,
//            (min + diff / 20 * 20).toInt -> 0x35319eFF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case "GreyToBlack" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0xffffffFF,
//            (min + diff / 20 * 2).toInt -> 0xf2f2f2FF,
//            (min + diff / 20 * 3).toInt -> 0xe4e4e4FF,
//            (min + diff / 20 * 4).toInt -> 0xd7d7d7FF,
//            (min + diff / 20 * 5).toInt -> 0xc9c9c9FF,
//            (min + diff / 20 * 6).toInt -> 0xbcbcbcFF,
//            (min + diff / 20 * 7).toInt -> 0xaeaeaeFF,
//            (min + diff / 20 * 8).toInt -> 0xa1a1a1FF,
//            (min + diff / 20 * 9).toInt -> 0x949494FF,
//            (min + diff / 20 * 10).toInt -> 0x868686FF,
//            (min + diff / 20 * 11).toInt -> 0x797979FF,
//            (min + diff / 20 * 12).toInt -> 0x6b6b6bFF,
//            (min + diff / 20 * 13).toInt -> 0x5e5e5eFF,
//            (min + diff / 20 * 14).toInt -> 0x515151FF,
//            (min + diff / 20 * 15).toInt -> 0x434343FF,
//            (min + diff / 20 * 16).toInt -> 0x363636FF,
//            (min + diff / 20 * 17).toInt -> 0x282828FF,
//            (min + diff / 20 * 18).toInt -> 0x1b1b1bFF,
//            (min + diff / 20 * 19).toInt -> 0x0d0d0dFF,
//            (min + diff / 20 * 20).toInt -> 0x000000FF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case "Elevation1" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0xaff0e9FF,
//            (min + diff / 20 * 2).toInt -> 0xb0f5beFF,
//            (min + diff / 20 * 3).toInt -> 0xd6fbb2FF,
//            (min + diff / 20 * 4).toInt -> 0xe3f297FF,
//            (min + diff / 20 * 5).toInt -> 0x6bc348FF,
//            (min + diff / 20 * 6).toInt -> 0x109433FF,
//            (min + diff / 20 * 7).toInt -> 0x348f3aFF,
//            (min + diff / 20 * 8).toInt -> 0x8fa92fFF,
//            (min + diff / 20 * 9).toInt -> 0xeac424FF,
//            (min + diff / 20 * 10).toInt -> 0xd17914FF,
//            (min + diff / 20 * 11).toInt -> 0xa52e07FF,
//            (min + diff / 20 * 12).toInt -> 0x800301FF,
//            (min + diff / 20 * 13).toInt -> 0x7e1b08FF,
//            (min + diff / 20 * 14).toInt -> 0x7b320eFF,
//            (min + diff / 20 * 15).toInt -> 0x84522cFF,
//            (min + diff / 20 * 16).toInt -> 0x9b7f6aFF,
//            (min + diff / 20 * 17).toInt -> 0xb2aca8FF,
//            (min + diff / 20 * 18).toInt -> 0xcbcbcbFF,
//            (min + diff / 20 * 19).toInt -> 0xe4e4e5FF,
//            (min + diff / 20 * 20).toInt -> 0xfdfdfeFF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case "Elevation2" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0x267300FF,
//            (min + diff / 20 * 2).toInt -> 0x3d9100FF,
//            (min + diff / 20 * 3).toInt -> 0x59af00FF,
//            (min + diff / 20 * 4).toInt -> 0x7acd00FF,
//            (min + diff / 20 * 5).toInt -> 0x9fe700FF,
//            (min + diff / 20 * 6).toInt -> 0xbfee00FF,
//            (min + diff / 20 * 7).toInt -> 0xe0f400FF,
//            (min + diff / 20 * 8).toInt -> 0xfbf300FF,
//            (min + diff / 20 * 9).toInt -> 0xfcde0dFF,
//            (min + diff / 20 * 10).toInt -> 0xf6cf2dFF,
//            (min + diff / 20 * 11).toInt -> 0xefc54bFF,
//            (min + diff / 20 * 12).toInt -> 0xe8bf68FF,
//            (min + diff / 20 * 13).toInt -> 0xdcb05dFF,
//            (min + diff / 20 * 14).toInt -> 0xcc9a3bFF,
//            (min + diff / 20 * 15).toInt -> 0xbc861eFF,
//            (min + diff / 20 * 16).toInt -> 0xac7405FF,
//            (min + diff / 20 * 17).toInt -> 0xbb7427FF,
//            (min + diff / 20 * 18).toInt -> 0xd18963FF,
//            (min + diff / 20 * 19).toInt -> 0xe8b5abFF,
//            (min + diff / 20 * 20).toInt -> 0xffffffFF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case "Elevation3" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0x458a6fFF,
//            (min + diff / 20 * 2).toInt -> 0x4c987bFF,
//            (min + diff / 20 * 3).toInt -> 0x53a688FF,
//            (min + diff / 20 * 4).toInt -> 0x5ab494FF,
//            (min + diff / 20 * 5).toInt -> 0x61c2a1FF,
//            (min + diff / 20 * 6).toInt -> 0x6ecfacFF,
//            (min + diff / 20 * 7).toInt -> 0x8dd9b3FF,
//            (min + diff / 20 * 8).toInt -> 0xace4baFF,
//            (min + diff / 20 * 9).toInt -> 0xcbeec1FF,
//            (min + diff / 20 * 10).toInt -> 0xeaf9c9FF,
//            (min + diff / 20 * 11).toInt -> 0xf6f9c0FF,
//            (min + diff / 20 * 12).toInt -> 0xeeeca9FF,
//            (min + diff / 20 * 13).toInt -> 0xe4d993FF,
//            (min + diff / 20 * 14).toInt -> 0xd9c57eFF,
//            (min + diff / 20 * 15).toInt -> 0xcfb06bFF,
//            (min + diff / 20 * 16).toInt -> 0xd4ad7bFF,
//            (min + diff / 20 * 17).toInt -> 0xdfb698FF,
//            (min + diff / 20 * 18).toInt -> 0xeac6b8FF,
//            (min + diff / 20 * 19).toInt -> 0xf4dedbFF,
//            (min + diff / 20 * 20).toInt -> 0xffffffFF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case "YellowToGreen" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0xffffe5FF,
//            (min + diff / 20 * 2).toInt -> 0xfcfed2FF,
//            (min + diff / 20 * 3).toInt -> 0xf8fcc0FF,
//            (min + diff / 20 * 4).toInt -> 0xeff9b3FF,
//            (min + diff / 20 * 5).toInt -> 0xe2f4aaFF,
//            (min + diff / 20 * 6).toInt -> 0xd4eea1FF,
//            (min + diff / 20 * 7).toInt -> 0xc2e698FF,
//            (min + diff / 20 * 8).toInt -> 0xafde8fFF,
//            (min + diff / 20 * 9).toInt -> 0x99d586FF,
//            (min + diff / 20 * 10).toInt -> 0x83cb7dFF,
//            (min + diff / 20 * 11).toInt -> 0x6cc073FF,
//            (min + diff / 20 * 12).toInt -> 0x55b567FF,
//            (min + diff / 20 * 13).toInt -> 0x3fa95cFF,
//            (min + diff / 20 * 14).toInt -> 0x339951FF,
//            (min + diff / 20 * 15).toInt -> 0x268846FF,
//            (min + diff / 20 * 16).toInt -> 0x187b3fFF,
//            (min + diff / 20 * 17).toInt -> 0x096f3aFF,
//            (min + diff / 20 * 18).toInt -> 0x006235FF,
//            (min + diff / 20 * 19).toInt -> 0x00542fFF,
//            (min + diff / 20 * 20).toInt -> 0x004529FF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case "HellRed" =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0xFF,
//            (min + diff / 20 * 2).toInt -> 0xFF,
//            (min + diff / 20 * 3).toInt -> 0xFF,
//            (min + diff / 20 * 4).toInt -> 0xFF,
//            (min + diff / 20 * 5).toInt -> 0xFF,
//            (min + diff / 20 * 6).toInt -> 0xFF,
//            (min + diff / 20 * 7).toInt -> 0xFF,
//            (min + diff / 20 * 8).toInt -> 0xFF,
//            (min + diff / 20 * 9).toInt -> 0xFF,
//            (min + diff / 20 * 10).toInt -> 0xFF,
//            (min + diff / 20 * 11).toInt -> 0xFF,
//            (min + diff / 20 * 12).toInt -> 0xFF,
//            (min + diff / 20 * 13).toInt -> 0xFF,
//            (min + diff / 20 * 14).toInt -> 0xFF,
//            (min + diff / 20 * 15).toInt -> 0xFF,
//            (min + diff / 20 * 16).toInt -> 0xFF,
//            (min + diff / 20 * 17).toInt -> 0xFF,
//            (min + diff / 20 * 18).toInt -> 0xFF,
//            (min + diff / 20 * 19).toInt -> 0xFF,
//            (min + diff / 20 * 20).toInt -> 0xFF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case _ =>
//        ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / 20 * 1).toInt -> 0x440154FF,
//            (min + diff / 20 * 2).toInt -> 0x481568FF,
//            (min + diff / 20 * 3).toInt -> 0x482677FF,
//            (min + diff / 20 * 4).toInt -> 0x453782FF,
//            (min + diff / 20 * 5).toInt -> 0x404789FF,
//            (min + diff / 20 * 6).toInt -> 0x39568cFF,
//            (min + diff / 20 * 7).toInt -> 0x33648eFF,
//            (min + diff / 20 * 8).toInt -> 0x2d718fFF,
//            (min + diff / 20 * 9).toInt -> 0x287e8fFF,
//            (min + diff / 20 * 10).toInt -> 0x238a8eFF,
//            (min + diff / 20 * 11).toInt -> 0x1f978cFF,
//            (min + diff / 20 * 12).toInt -> 0x20a487FF,
//            (min + diff / 20 * 13).toInt -> 0x2ab080FF,
//            (min + diff / 20 * 14).toInt -> 0x3cbc75FF,
//            (min + diff / 20 * 15).toInt -> 0x56c767FF,
//            (min + diff / 20 * 16).toInt -> 0x74d155FF,
//            (min + diff / 20 * 17).toInt -> 0x95d940FF,
//            (min + diff / 20 * 18).toInt -> 0xb9df29FF,
//            (min + diff / 20 * 19).toInt -> 0xdde419FF,
//            (min + diff / 20 * 20).toInt -> 0xfee825FF,
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//    }
//  }
//
//
//  /**
//   * TODO 自定义色带，不知道scala的map怎么返回代码段，后面再改，先这样能用。。。。。。
//   *
//   * @param colors
//   * @param min
//   * @param diff
//   * @param noDataColor
//   * @param fallbackColor
//   * @return
//   */
//  def customColorRamp(colors: Array[Array[Int]], min: Double, diff: Double, noDataColor: Array[Int], fallbackColor: Array[Int]): ColorMap = {
//    var i = 0
//    val length = colors.length
//    length match {
//      case 2 =>
//        return ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / length * 1).toInt -> geotrellis.raster.render.RGBA(colors(0)(0), colors(0)(1), colors(0)(2), colors(0)(3)),
//            (min + diff / length * 2).toInt -> geotrellis.raster.render.RGBA(colors(1)(0), colors(1)(1), colors(1)(2), colors(1)(3)),
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case 3 =>
//        return ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / length * 1).toInt -> geotrellis.raster.render.RGBA(colors(0)(0), colors(0)(1), colors(0)(2), colors(0)(3)),
//            (min + diff / length * 2).toInt -> geotrellis.raster.render.RGBA(colors(1)(0), colors(1)(1), colors(1)(2), colors(1)(3)),
//            (min + diff / length * 3).toInt -> geotrellis.raster.render.RGBA(colors(2)(0), colors(2)(1), colors(2)(2), colors(2)(3)),
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case 4 =>
//        return ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / length * 1).toInt -> geotrellis.raster.render.RGBA(colors(0)(0), colors(0)(1), colors(0)(2), colors(0)(3)),
//            (min + diff / length * 2).toInt -> geotrellis.raster.render.RGBA(colors(1)(0), colors(1)(1), colors(1)(2), colors(1)(3)),
//            (min + diff / length * 3).toInt -> geotrellis.raster.render.RGBA(colors(2)(0), colors(2)(1), colors(2)(2), colors(2)(3)),
//            (min + diff / length * 4).toInt -> geotrellis.raster.render.RGBA(colors(3)(0), colors(3)(1), colors(3)(2), colors(3)(3)),
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case 5 =>
//        return ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / length * 1).toInt -> geotrellis.raster.render.RGBA(colors(0)(0), colors(0)(1), colors(0)(2), colors(0)(3)),
//            (min + diff / length * 2).toInt -> geotrellis.raster.render.RGBA(colors(1)(0), colors(1)(1), colors(1)(2), colors(1)(3)),
//            (min + diff / length * 3).toInt -> geotrellis.raster.render.RGBA(colors(2)(0), colors(2)(1), colors(2)(2), colors(2)(3)),
//            (min + diff / length * 4).toInt -> geotrellis.raster.render.RGBA(colors(3)(0), colors(3)(1), colors(3)(2), colors(3)(3)),
//            (min + diff / length * 5).toInt -> geotrellis.raster.render.RGBA(colors(4)(0), colors(4)(1), colors(4)(2), colors(4)(3)),
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case 6 =>
//        return ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / length * 1).toInt -> geotrellis.raster.render.RGBA(colors(0)(0), colors(0)(1), colors(0)(2), colors(0)(3)),
//            (min + diff / length * 2).toInt -> geotrellis.raster.render.RGBA(colors(1)(0), colors(1)(1), colors(1)(2), colors(1)(3)),
//            (min + diff / length * 3).toInt -> geotrellis.raster.render.RGBA(colors(2)(0), colors(2)(1), colors(2)(2), colors(2)(3)),
//            (min + diff / length * 4).toInt -> geotrellis.raster.render.RGBA(colors(3)(0), colors(3)(1), colors(3)(2), colors(3)(3)),
//            (min + diff / length * 5).toInt -> geotrellis.raster.render.RGBA(colors(4)(0), colors(4)(1), colors(4)(2), colors(4)(3)),
//            (min + diff / length * 6).toInt -> geotrellis.raster.render.RGBA(colors(5)(0), colors(5)(1), colors(5)(2), colors(5)(3)),
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case 7 =>
//        return ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / length * 1).toInt -> geotrellis.raster.render.RGBA(colors(0)(0), colors(0)(1), colors(0)(2), colors(0)(3)),
//            (min + diff / length * 2).toInt -> geotrellis.raster.render.RGBA(colors(1)(0), colors(1)(1), colors(1)(2), colors(1)(3)),
//            (min + diff / length * 3).toInt -> geotrellis.raster.render.RGBA(colors(2)(0), colors(2)(1), colors(2)(2), colors(2)(3)),
//            (min + diff / length * 4).toInt -> geotrellis.raster.render.RGBA(colors(3)(0), colors(3)(1), colors(3)(2), colors(3)(3)),
//            (min + diff / length * 5).toInt -> geotrellis.raster.render.RGBA(colors(4)(0), colors(4)(1), colors(4)(2), colors(4)(3)),
//            (min + diff / length * 6).toInt -> geotrellis.raster.render.RGBA(colors(5)(0), colors(5)(1), colors(5)(2), colors(5)(3)),
//            (min + diff / length * 7).toInt -> geotrellis.raster.render.RGBA(colors(6)(0), colors(6)(1), colors(6)(2), colors(6)(3)),
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case 8 =>
//        return ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / length * 1).toInt -> geotrellis.raster.render.RGBA(colors(0)(0), colors(0)(1), colors(0)(2), colors(0)(3)),
//            (min + diff / length * 2).toInt -> geotrellis.raster.render.RGBA(colors(1)(0), colors(1)(1), colors(1)(2), colors(1)(3)),
//            (min + diff / length * 3).toInt -> geotrellis.raster.render.RGBA(colors(2)(0), colors(2)(1), colors(2)(2), colors(2)(3)),
//            (min + diff / length * 4).toInt -> geotrellis.raster.render.RGBA(colors(3)(0), colors(3)(1), colors(3)(2), colors(3)(3)),
//            (min + diff / length * 5).toInt -> geotrellis.raster.render.RGBA(colors(4)(0), colors(4)(1), colors(4)(2), colors(4)(3)),
//            (min + diff / length * 6).toInt -> geotrellis.raster.render.RGBA(colors(5)(0), colors(5)(1), colors(5)(2), colors(5)(3)),
//            (min + diff / length * 7).toInt -> geotrellis.raster.render.RGBA(colors(6)(0), colors(6)(1), colors(6)(2), colors(6)(3)),
//            (min + diff / length * 8).toInt -> geotrellis.raster.render.RGBA(colors(7)(0), colors(7)(1), colors(7)(2), colors(7)(3)),
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case 9 =>
//        return ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / length * 1).toInt -> geotrellis.raster.render.RGBA(colors(0)(0), colors(0)(1), colors(0)(2), colors(0)(3)),
//            (min + diff / length * 2).toInt -> geotrellis.raster.render.RGBA(colors(1)(0), colors(1)(1), colors(1)(2), colors(1)(3)),
//            (min + diff / length * 3).toInt -> geotrellis.raster.render.RGBA(colors(2)(0), colors(2)(1), colors(2)(2), colors(2)(3)),
//            (min + diff / length * 4).toInt -> geotrellis.raster.render.RGBA(colors(3)(0), colors(3)(1), colors(3)(2), colors(3)(3)),
//            (min + diff / length * 5).toInt -> geotrellis.raster.render.RGBA(colors(4)(0), colors(4)(1), colors(4)(2), colors(4)(3)),
//            (min + diff / length * 6).toInt -> geotrellis.raster.render.RGBA(colors(5)(0), colors(5)(1), colors(5)(2), colors(5)(3)),
//            (min + diff / length * 7).toInt -> geotrellis.raster.render.RGBA(colors(6)(0), colors(6)(1), colors(6)(2), colors(6)(3)),
//            (min + diff / length * 8).toInt -> geotrellis.raster.render.RGBA(colors(7)(0), colors(7)(1), colors(7)(2), colors(7)(3)),
//            (min + diff / length * 9).toInt -> geotrellis.raster.render.RGBA(colors(8)(0), colors(8)(1), colors(8)(2), colors(8)(3)),
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//      case 10 =>
//        return ColorMap(
//          scala.Predef.Map(
//            (min).toInt -> geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)),
//            (min + diff / length * 1).toInt -> geotrellis.raster.render.RGBA(colors(0)(0), colors(0)(1), colors(0)(2), colors(0)(3)),
//            (min + diff / length * 2).toInt -> geotrellis.raster.render.RGBA(colors(1)(0), colors(1)(1), colors(1)(2), colors(1)(3)),
//            (min + diff / length * 3).toInt -> geotrellis.raster.render.RGBA(colors(2)(0), colors(2)(1), colors(2)(2), colors(2)(3)),
//            (min + diff / length * 4).toInt -> geotrellis.raster.render.RGBA(colors(3)(0), colors(3)(1), colors(3)(2), colors(3)(3)),
//            (min + diff / length * 5).toInt -> geotrellis.raster.render.RGBA(colors(4)(0), colors(4)(1), colors(4)(2), colors(4)(3)),
//            (min + diff / length * 6).toInt -> geotrellis.raster.render.RGBA(colors(5)(0), colors(5)(1), colors(5)(2), colors(5)(3)),
//            (min + diff / length * 7).toInt -> geotrellis.raster.render.RGBA(colors(6)(0), colors(6)(1), colors(6)(2), colors(6)(3)),
//            (min + diff / length * 8).toInt -> geotrellis.raster.render.RGBA(colors(7)(0), colors(7)(1), colors(7)(2), colors(7)(3)),
//            (min + diff / length * 9).toInt -> geotrellis.raster.render.RGBA(colors(8)(0), colors(8)(1), colors(8)(2), colors(8)(3)),
//            (min + diff / length * 10).toInt -> geotrellis.raster.render.RGBA(colors(9)(0), colors(9)(1), colors(9)(2), colors(9)(3)),
//            -1 -> geotrellis.raster.render.RGBA(255, 255, 255, 255)
//          ),
//          ColorMap.Options(
//            classBoundaryType = LessThanOrEqualTo,
//            noDataColor = geotrellis.raster.render.RGBA(noDataColor(0), noDataColor(1), noDataColor(2), noDataColor(3)), // transparent
//            fallbackColor = geotrellis.raster.render.RGBA(fallbackColor(0), fallbackColor(1), fallbackColor(2), fallbackColor(3)), // transparent
//            strict = false
//          )
//        )
//
//    }
//  }
//}
//
//
//
