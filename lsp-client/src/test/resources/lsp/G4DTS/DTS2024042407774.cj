/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package restdemo.controller

from net import http.Cookie
from cax_rs import rest.RestContext
from cax_rs import rest.RestSession
from std import time.DateTime
import restdemo.model.*
from rest_common import rest.common.log.LogFactory
from rest_server import rest.server.macros.*
from rest_server import rest.server.export.*

@Rest["/demo/test"]
public class DemoController {
    private static let logger = LogFactory.getInstance()

    @Get["/gettest"]
    @QueryParameter[name ="id"]
    @QueryParameter[name ="str", defaultValue ="defaultStr", maxLength = 10, minLength = 2, pattern ="\\D*"]
    @QueryParameter[name ="isValid"]
    @QueryParameter[name ="count", max = 100000, min = 0]
    @QueryParameter[name ="totalMoney", max = 999999.2, min = 0.1]
    public func get(id: String, str: Option<String>, isValid: Bool, count: Option<Int64>, totalMoney: Float64): String {
        println("_________________get")

        let res = "get response: id:${id}, str:${str.getOrThrow()}, isValid:${isValid}, count:${count}, totalMoney:${totalMoney}"
        return res
    }

    @Get["/gettest/PathParameter/:pathId/:pathStr/:pathBool"]
    @PathParameter[name ="pathId", max = 9999, min = 1]
    @PathParameter[name ="pathStr", maxLength = 999, min = 1]
    @PathParameter[name ="pathBool"]
    public func get2(pathId: Int64, pathStr: String, pathBool: Bool): String {
        println("_________________get2")

        let res = pathId.toString() + pathStr + pathBool.toString()
        return res
    }

    @Get["/gettest/HeaderParameter"]
    @HeaderParameter[name ="header1", max = 9999, min = 1]
    @HeaderParameter[name ="header2", maxLength = 999, min = 1, pattern ="\\D*"]
    @HeaderParameter[name ="header3"]
    public func get3(header1: Int64, header2: String, header3: Option<Bool>): String {
        println("_________________get3")

        let res = header1.toString() + header2 + header3.toString()
        return res
    }

    @Get["/gettest/CookieParameter"]
    @CookieParameter[name ="cookie1", max = 9999, min = 1]
    @CookieParameter[name ="cookie2", maxLength = 3, min = 1, pattern ="\\D*"]
    @CookieParameter[name ="cookie3"]
    public func get4(cookie1: Int64, cookie2: String, cookie3: Bool): String {
        println("_________________get4")

        let res = cookie1.toString() + cookie2 + cookie3.toString()
        return res
    }

    @Get["/gettest/ContextParameter"]
    @Context
    public func get5(context: RestContext): String {
        return context.request.url.toString()
    }

    @Post["/posttest"]
    @RequestBody
    public func post(person: Person): Person {
        println("_________________post")
        return person
    }

    @Post["/posttest2"]
    @Consumes["application/json","application/xml"]
    @Produces["application/json"]
    @RequestBody
    public func post2(person: Person): Person {
        println("_________________post2")
        return person
    }

    @Post["/posttest3"]
    @QueryParameter[name ="id"]
    @RequestBody
    public func post3(id: String, person: Person): String {
        println("_________________post")
        return person.toString() + "___" + id
    }

    @Post["/SaveBackTrackList"]
    @RequestBody
    public func savetest(saveBackTrackList: SaveBackTrackList): RetModel {
        logger.debug("_____________________________________________________________test log performance")

        var retModel = RetModel()
        retModel.msg = "成功返回"
        retModel.code = "0"
        retModel.success = true
        return retModel
    }

    @Get["/serializeTest256Byte"]
    @QueryParameter[name ="getResponse"]
    public func serializeTest256(getResponse: String): SaveBackTrackList {
        logger.debug("_____________________________________________________________test log performance")
        if (getResponse == "true") {
            return PerformanceData.getInstance().value256
        } else {
            serialize(PerformanceData.getInstance().value256)
            return SaveBackTrackList()
        }
    }

    @Get["/serializeTest2K"]
    @QueryParameter[name ="getResponse"]
    public func serializeTest2K(getResponse: String): SaveBackTrackList {
        logger.debug("_____________________________________________________________test log performance")
        if (getResponse == "true") {
            return PerformanceData.getInstance().value2K
        } else {
            serialize(PerformanceData.getInstance().value2K)
            return SaveBackTrackList()
        }
    }

    @Get["/serializeTest5K"]
    @QueryParameter[name ="getResponse"]
    public func serializeTest5K(getResponse: String): SaveBackTrackList {
        logger.debug("_____________________________________________________________test log performance")
        if (getResponse == "true") {
            return PerformanceData.getInstance().value5K
        } else {
            serialize(PerformanceData.getInstance().value5K)
            return SaveBackTrackList()
        }
    }

    @Get["/serializeTest16K"]
    @QueryParameter[name ="getResponse"]
    public func serializeTest16K(getResponse: String): SaveBackTrackList {
        logger.debug("_____________________________________________________________test log performance")
        if (getResponse == "true") {
            return PerformanceData.getInstance().value16K
        } else {
            serialize(PerformanceData.getInstance().value16K)
            return SaveBackTrackList()
        }
    }

    @Get["/serializeTest128K"]
    @QueryParameter[name ="getResponse"]
    public func serializeTest128K(getResponse: String): SaveBackTrackList {
        logger.debug("_____________________________________________________________test log performance")
        if (getResponse == "true") {
            return PerformanceData.getInstance().value128K
        } else {
            serialize(PerformanceData.getInstance().value128K)
            return SaveBackTrackList()
        }
    }

    @Get["/noDeserialize"]
    public func noDeserializeTest(): String {
        logger.debug("_____________________________________________________________test log performance")
        return "成功返回"
    }

    @Get["/cookieTest"]
    @Context
    public func setCookieTest(context: RestContext): String {
        var testCookie = Cookie("cookie3", DateTime.now().toString(), expires:Some(DateTime.now().addMinutes(1)))
        context.response.addCookie(testCookie)
        return "成功返回"
    }

    @Get["/sessionTest"]
    @Context