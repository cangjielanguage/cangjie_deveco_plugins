/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package question.solution
from ohos import router.*
from encoding import json.*
from ohos import base.*
from ohos import component.*
from ohos import state_manage.*
from ohos import state_macro_manage.*
import appres.*
import utils.*
import ui.*

@Component
public class SolutionList {
    var solutions: SolutionDataSource = SolutionDataSource(Solution.fakeList())
    func render() {
        Column() {
            List( 4) {
                LazyForEach(
                    solutions,
                    itemGeneratorFunc: {
                        solution: Solution, index: Int => ListItem() {
                            cell(solution)
                        }
                    }
                )
            }
        }
    }

    @Builder
    func cell(data: Solution) {
        Column(12) {
            Row(8) {
                Image(data.author.userAvatar).size(width: 20, height: 20).borderRadius(10)
                Text(data.author.realName).maxLines(1).fontSize(13).fontColor(CC.label_secondary)
                Blank().layoutWeight(1)
                Text("2020-10-02").fontSize(13).fontColor(CC.grey2)
            }
            Flex() {
                Text(data.title).maxLines(1).fontSize(17).fontWeight(FontWeight.Medium).fontColor(
                    CC.label_primary).textOverflow(TextOverflow.Ellipsis)
                Blank().width(10)
                if (data.byLeetcode) {
                    LeetcodeTag()
                }
                Blank()
                .flexGrow(1)
            }
            Text(data.summary).fontSize(15).fontColor(CC.label_secondary).lineHeight(20).maxLines(3)
            if (let Some(videoCover) <- data.videoCover) {
                Image(videoCover).width(100.percent).borderRadius(5).aspectRatio(1.77)
            } else if (let Some(thumbnail) <- data.thumbnail) {
                Image(thumbnail).width(100.percent).aspectRatio(1.77)
            }
            Row(4) {
                Text("有用").fontSize(13).fontColor(CC.label_tertiary)
                Text("${data.favoriteCount}").margin(right: 14).fontSize(13).fontColor( //useUnitK
                    CC.label_tertiary)
                Text("评论").fontSize(13).fontColor(CC.label_tertiary)
                Text("${data.commentCount}").fontSize(13).fontColor(CC.label_tertiary) //useUnitK
                Blank().layoutWeight(1)
            }
        }.width(100.percent).padding(20).alignItems(HorizontalAlign.Start).onClick({
            e =>
            Router.push(url: "SolutionDetailPage")
        //          router.pushUrl({
        //            url: 'question/pages/SolutionDetailPage',
        //            params: { solution: data, questionSlug: this.questionSlug }
        //          })
        })
    }
}
