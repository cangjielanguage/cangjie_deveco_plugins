/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

var rename: int32 = 100
var test1: bool = false;
var name: bool = true;

var num:float32 = 6.0
record Record {
    var rename: int32 = 100
    var width: float32 = 12.456
    var length: int32 = 123
    var size: int32 = 456
}


/*测试注释*/
class Demo2 {
    var rename: int32 = Ox100
    var width: int32 = rename
    var length: int32 = rename
    var size: int32 = rename
    var str: String = '''aaaaaaa'''
    var str1: String = """test demo"""
    func add(): int32 {
  //  var aa: bool = true;
  //  var bb: bool = false;
        return 100
    }
}
/**
 * test function
 */
func main(): int64 {
    var demo: Demo2 = Demo2()
    

    var chartest:char = 'A'
    var testString2 : String

    testString2 = "Hello world..."
    let testString5 = "Char Lang"
    //let testString6 = testString2 + testString5

    let testarr = {1, 2, 3, 4, 5}
    for i in testarr {
        i = i + 5
    }
    return 0;
}

