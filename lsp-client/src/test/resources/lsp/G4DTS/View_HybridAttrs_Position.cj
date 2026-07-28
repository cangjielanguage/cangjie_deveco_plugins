/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package ohos_app

import ohos.state_macro_manage_ndk.*

@HybridComponentEntry
@Component
class View_HybridAttrs_Position {
    @LocalStorageProp["targetView", "InterOp"]
    let targetView: String = ""

    @State
    var parentHeight: Length = 800.vp
    @State
    var parentWidth: Length = 400.vp
    @State
    var parentBackGroundColor: Color = Color(0xE1FFFF)
    @State
    var BackGroundColor: Color = Color.GREEN
    @State
    var TouchHotZoneText: String = ""
    @State
    var RenderFitWidth: Length = 60.percent
    @State
    var RenderFitHeight: Length = 80.vp
    @State
    var RenderFitWidth1: Length = 80.percent
    @State
    var RenderFitHeight1: Length = 100.vp
    @State
    var RenderFitWidth2: Length = 60.percent
    @State
    var RenderFitHeight2: Length = 80.vp
    @State
    var RenderFitFlag: Bool = true

    let scroller = Scroller()
    let swiperController: SwiperController = SwiperController()
    @State
    var isRefreshing: Bool = false
    let renderFlag = false //跳过if
    let Colors = [Color(0x98FB98), Color(0x317aff), Color(0xFFD700), Color(0xFFE4E1), Color(0xAB82FF)]

    func build() {
        Scroll() {
            Column() {
                
if (this.targetView != "Badge"){
                LazyForEach(
                    Pixel_PropDataSource(Pixel_Prop_List),
                    itemGeneratorFunc: {
                        Pixel_Prop: (String, Any, String), idx: Int64 => if (this.renderFlag) {}
                        else if(this.targetView == "AlphabetIndexer"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {AlphabetIndexer(arrayValue: ["#", "A", "B", "C", "D"], selected: 0)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {AlphabetIndexer(arrayValue: ["#", "A", "B", "C", "D"], selected: 0)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {AlphabetIndexer(arrayValue: ["#", "A", "B", "C", "D"], selected: 0)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {AlphabetIndexer(arrayValue: ["#", "A", "B", "C", "D"], selected: 0)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "Blank"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {Blank(min: 10)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {Blank(min: 10)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {Blank(min: 10)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {Blank(min: 10)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "Button"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {Button{Text("Button").width(80.percent).height(80.percent).backgroundColor(Color.BLUE)}
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {Button{Text("Button").width(80.percent).height(80.percent).backgroundColor(Color.BLUE)}
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {Button{Text("Button").width(80.percent).height(80.percent).backgroundColor(Color.BLUE)}
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {Button{Text("Button").width(80.percent).height(80.percent).backgroundColor(Color.BLUE)}
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "Divider"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {Divider()
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {Divider()
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {Divider()
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {Divider()
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "Image"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {Image(@r(app.media.startIcon)).objectFit(ImageFit.Contain)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {Image(@r(app.media.startIcon)).objectFit(ImageFit.Contain)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {Image(@r(app.media.startIcon)).objectFit(ImageFit.Contain)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {Image(@r(app.media.startIcon)).objectFit(ImageFit.Contain)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "LoadingProgress"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {LoadingProgress()
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {LoadingProgress()
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {LoadingProgress()
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {LoadingProgress()
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "Refresh"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {Refresh(
RefreshParams(
refreshing: @Binder(this.isRefreshing),offset: 16.vp,friction: 32)) {Text("1").width(80.percent).height(80.percent).backgroundColor(this.Colors[1])}
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {Refresh(
RefreshParams(
refreshing: @Binder(this.isRefreshing),offset: 16.vp,friction: 32)) {Text("1").width(80.percent).height(80.percent).backgroundColor(this.Colors[1])}
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {Refresh(
RefreshParams(
refreshing: @Binder(this.isRefreshing),offset: 16.vp,friction: 32)) {Text("1").width(80.percent).height(80.percent).backgroundColor(this.Colors[1])}
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {Refresh(
RefreshParams(
refreshing: @Binder(this.isRefreshing),offset: 16.vp,friction: 32)) {Text("1").width(80.percent).height(80.percent).backgroundColor(this.Colors[1])}
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "Search"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {Search(value: "", placeholder: "Type to search")
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {Search(value: "", placeholder: "Type to search")
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {Search(value: "", placeholder: "Type to search")
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {Search(value: "", placeholder: "Type to search")
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "Text"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {Text(targetView)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {Text(targetView)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {Text(targetView)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {Text(targetView)
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "TextInput"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {TextInput(placeholder: "TextInput")
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {TextInput(placeholder: "TextInput")
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {TextInput(placeholder: "TextInput")
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {TextInput(placeholder: "TextInput")
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "TextPicker"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {TextPicker(["apple", "banana", "orange", "peach"])
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {TextPicker(["apple", "banana", "orange", "peach"])
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {TextPicker(["apple", "banana", "orange", "peach"])
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {TextPicker(["apple", "banana", "orange", "peach"])
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "Select"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {Select([SelectOption("aaa", icon: @r(app.media.startIcon)), SelectOption("bbb", icon: @r(app.media.startIcon))]).setValue("Select")
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {Select([SelectOption("aaa", icon: @r(app.media.startIcon)), SelectOption("bbb", icon: @r(app.media.startIcon))]).setValue("Select")
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {Select([SelectOption("aaa", icon: @r(app.media.startIcon)), SelectOption("bbb", icon: @r(app.media.startIcon))]).setValue("Select")
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {Select([SelectOption("aaa", icon: @r(app.media.startIcon)), SelectOption("bbb", icon: @r(app.media.startIcon))]).setValue("Select")
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "Column"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {
Column() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {
Column() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {
Column() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {
Column() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "Grid"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {Grid {
            GridItem {
                Text("Message").width(80.percent).height(80.percent).backgroundColor(this.Colors[2])
            }.width(80.percent).height(80.percent).backgroundColor(this.Colors[1])
             }
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
             .width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    else if (Pixel_Prop[1] is Float64) {Grid {
            GridItem {
                Text("Message").width(80.percent).height(80.percent).backgroundColor(this.Colors[2])
            }.width(80.percent).height(80.percent).backgroundColor(this.Colors[1])
             }
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
             .width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    else if (Pixel_Prop[1] is Int64) {Grid {
            GridItem {
                Text("Message").width(80.percent).height(80.percent).backgroundColor(this.Colors[2])
            }.width(80.percent).height(80.percent).backgroundColor(this.Colors[1])
             }
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
             .width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    else if (Pixel_Prop[1] is CJResource) {Grid {
            GridItem {
                Text("Message").width(80.percent).height(80.percent).backgroundColor(this.Colors[2])
            }.width(80.percent).height(80.percent).backgroundColor(this.Colors[1])
             }
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
             .width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    }
                            else if(this.targetView == "GridItem"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {Grid {
            GridItem {
                Text("Message").width(80.percent).height(80.percent).backgroundColor(this.Colors[2])
            }
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                 .width(80.percent).height(100)
                 .backgroundColor(this.Colors[1])
                 }.width(80.percent).height(100).backgroundColor(this.Colors[0])
         }
                                    else if (Pixel_Prop[1] is Float64) {Grid {
            GridItem {
                Text("Message").width(80.percent).height(80.percent).backgroundColor(this.Colors[2])
            }
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                 .width(80.percent).height(100)
                 .backgroundColor(this.Colors[1])
                 }.width(80.percent).height(100).backgroundColor(this.Colors[0])
         }
                                    else if (Pixel_Prop[1] is Int64) {Grid {
            GridItem {
                Text("Message").width(80.percent).height(80.percent).backgroundColor(this.Colors[2])
            }
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                 .width(80.percent).height(100)
                 .backgroundColor(this.Colors[1])
                 }.width(80.percent).height(100).backgroundColor(this.Colors[0])
         }
                                    else if (Pixel_Prop[1] is CJResource) {Grid {
            GridItem {
                Text("Message").width(80.percent).height(80.percent).backgroundColor(this.Colors[2])
            }
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                 .width(80.percent).height(100)
                 .backgroundColor(this.Colors[1])
                 }.width(80.percent).height(100).backgroundColor(this.Colors[0])
         }
                                    }
                            else if(this.targetView == "Flex"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {Flex(FlexParams()) {
Text("1").height(80.percent).width(80.percent).backgroundColor(this.Colors[1])
}
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {Flex(FlexParams()) {
Text("1").height(80.percent).width(80.percent).backgroundColor(this.Colors[1])
}
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {Flex(FlexParams()) {
Text("1").height(80.percent).width(80.percent).backgroundColor(this.Colors[1])
}
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {Flex(FlexParams()) {
Text("1").height(80.percent).width(80.percent).backgroundColor(this.Colors[1])
}
    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "List"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {List() {
            ListItemGroup() {
                ListItem() {
                    Text("Text").width(80.percent).height(80.percent).backgroundColor(this.Colors[3])
                }.width(80.percent).height(80.percent).backgroundColor(this.Colors[2])}.width(80.percent).height(80.percent).backgroundColor(this.Colors[1])}
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    .width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    else if (Pixel_Prop[1] is Float64) {List() {
            ListItemGroup() {
                ListItem() {
                    Text("Text").width(80.percent).height(80.percent).backgroundColor(this.Colors[3])
                }.width(80.percent).height(80.percent).backgroundColor(this.Colors[2])}.width(80.percent).height(80.percent).backgroundColor(this.Colors[1])}
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    .width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    else if (Pixel_Prop[1] is Int64) {List() {
            ListItemGroup() {
                ListItem() {
                    Text("Text").width(80.percent).height(80.percent).backgroundColor(this.Colors[3])
                }.width(80.percent).height(80.percent).backgroundColor(this.Colors[2])}.width(80.percent).height(80.percent).backgroundColor(this.Colors[1])}
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    .width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    else if (Pixel_Prop[1] is CJResource) {List() {
            ListItemGroup() {
                ListItem() {
                    Text("Text").width(80.percent).height(80.percent).backgroundColor(this.Colors[3])
                }.width(80.percent).height(80.percent).backgroundColor(this.Colors[2])}.width(80.percent).height(80.percent).backgroundColor(this.Colors[1])}
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    .width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    }
                            else if(this.targetView == "ListItem"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {List() {
            ListItemGroup() {
                ListItem() {
                    Text("Text").width(80.percent).height(80.percent).backgroundColor(this.Colors[3])
                }
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    .width(80.percent).height(100).backgroundColor(this.Colors[2])}.width(80.percent).height(80.percent).backgroundColor(this.Colors[1])}.width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    else if (Pixel_Prop[1] is Float64) {List() {
            ListItemGroup() {
                ListItem() {
                    Text("Text").width(80.percent).height(80.percent).backgroundColor(this.Colors[3])
                }
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    .width(80.percent).height(100).backgroundColor(this.Colors[2])}.width(80.percent).height(80.percent).backgroundColor(this.Colors[1])}.width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    else if (Pixel_Prop[1] is Int64) {List() {
            ListItemGroup() {
                ListItem() {
                    Text("Text").width(80.percent).height(80.percent).backgroundColor(this.Colors[3])
                }
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    .width(80.percent).height(100).backgroundColor(this.Colors[2])}.width(80.percent).height(80.percent).backgroundColor(this.Colors[1])}.width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    else if (Pixel_Prop[1] is CJResource) {List() {
            ListItemGroup() {
                ListItem() {
                    Text("Text").width(80.percent).height(80.percent).backgroundColor(this.Colors[3])
                }
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    .width(80.percent).height(100).backgroundColor(this.Colors[2])}.width(80.percent).height(80.percent).backgroundColor(this.Colors[1])}.width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    }
                            else if(this.targetView == "ListItemGroup"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {List() {
            ListItemGroup() {
                ListItem() {
                    Text("Text").width(80.percent).height(80.percent).backgroundColor(this.Colors[3])
                }.width(80.percent).height(80.percent).backgroundColor(this.Colors[2])}
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    .width(80.percent).height(100).backgroundColor(this.Colors[1])}.width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    else if (Pixel_Prop[1] is Float64) {List() {
            ListItemGroup() {
                ListItem() {
                    Text("Text").width(80.percent).height(80.percent).backgroundColor(this.Colors[3])
                }.width(80.percent).height(80.percent).backgroundColor(this.Colors[2])}
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    .width(80.percent).height(100).backgroundColor(this.Colors[1])}.width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    else if (Pixel_Prop[1] is Int64) {List() {
            ListItemGroup() {
                ListItem() {
                    Text("Text").width(80.percent).height(80.percent).backgroundColor(this.Colors[3])
                }.width(80.percent).height(80.percent).backgroundColor(this.Colors[2])}
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    .width(80.percent).height(100).backgroundColor(this.Colors[1])}.width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    else if (Pixel_Prop[1] is CJResource) {List() {
            ListItemGroup() {
                ListItem() {
                    Text("Text").width(80.percent).height(80.percent).backgroundColor(this.Colors[3])
                }.width(80.percent).height(80.percent).backgroundColor(this.Colors[2])}
    .id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    .width(80.percent).height(100).backgroundColor(this.Colors[1])}.width(80.percent).height(100).backgroundColor(this.Colors[0])}
                                    }
                            else if(this.targetView == "Row"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {
Row() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {
Row() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {
Row() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {
Row() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "Scroll"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {
Scroll() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {
Scroll() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {
Scroll() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {
Scroll() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "Stack"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {
Stack() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {
Stack() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {
Stack() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {
Stack() {
Text("1").height(50.percent).width(50.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "Swiper"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {
Swiper(this.swiperController) {
Text("1")
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {
Swiper(this.swiperController) {
Text("1")
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {
Swiper(this.swiperController) {
Text("1")
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {
Swiper(this.swiperController) {
Text("1")
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            else if(this.targetView == "RelativeContainer"){
                                if (this.renderFlag) {}
                                    else if (Pixel_Prop[1] is Length) {
RelativeContainer() {Text("Text").width(80.percent).height(10.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Length).getOrThrow() ,y:(Pixel_Prop[1] as Length).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Float64) {
RelativeContainer() {Text("Text").width(80.percent).height(10.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Float64).getOrThrow() ,y:(Pixel_Prop[1] as Float64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is Int64) {
RelativeContainer() {Text("Text").width(80.percent).height(10.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as Int64).getOrThrow() ,y:(Pixel_Prop[1] as Int64).getOrThrow())
    
                                            }
                                    else if (Pixel_Prop[1] is CJResource) {
RelativeContainer() {Text("Text").width(80.percent).height(10.percent).backgroundColor(this.Colors[1])
}

    .width(80.percent).height(100).backgroundColor(this.Colors[0]).id(this.targetView + Pixel_Prop[0]).position(x:(Pixel_Prop[1] as CJResource).getOrThrow() ,y:(Pixel_Prop[1] as CJResource).getOrThrow())
    
                                            }
                                    }
                            }
                )
            }
            


            }
        }.id("Scroll").height(this.parentHeight).width(this.parentWidth).backgroundColor(this.parentBackGroundColor)
    }
}

