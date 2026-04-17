## ADDED Requirements

### Requirement: 奖励配置管理
系统 SHALL 通过 `PrizeDao.getPrizesByGroupId(groupId)` 查询分组绑定的奖品，复用现有 `PrizeEntity` 数据结构。奖品信息包含 name、imagePath、level(1/2/3)。

### Requirement: 奖品弹窗展示
系统 SHALL 在排名计算完成后，以半透明弹窗形式展示前三名的奖品信息，并明确显示名次所属分组。

#### Scenario: 单分组计时 - 显示奖品弹窗
- **WHEN** 仅一个分组计时的排名前三计算完成
- **THEN** 系统显示单分组排名弹窗，包含排名、组名（如"第1名: 学习"）、时长和奖品信息

#### Scenario: 多分组同时计时 - 显示汇总弹窗
- **WHEN** 多个分组同时计时且全部停止后，所有分组的排名计算完成
- **THEN** 系统显示**汇总弹窗**，同时展示所有分组的排名及奖励情况，而非逐个弹出

#### Scenario: 奖品图标加载
- **WHEN** 弹窗需要显示奖品图标
- **THEN** 系统通过 `BitmapFactory.decodeFile(imagePath)` 从 `PrizeEntity.imagePath` 指定的本地文件路径加载图标，加载失败时显示默认占位图（礼物盒图标 `ic_reward_placeholder.xml`）

### Requirement: 汇总弹窗设计
系统 SHALL 提供汇总弹窗（`SummaryRewardDialog`），在多分组同时计时全部停止后，以一个弹窗同时展示所有分组的排名与奖励信息。

#### 数据模型
- `RewardUiState` SHALL 新增 `ShowSummary` 状态，包含 `items: List<SummaryRewardItem>`
- `SummaryRewardItem` 数据类 SHALL 包含：`groupId`、`groupName`、`currentDuration`、`rank`（Int?，前三名有值）、`rewardInfo`（RewardInfo?）、`isQualified`（Boolean）、`exceedsQualification`（Boolean，时长超过合格线）、`qualificationDuration`（Long?）

#### 弹窗布局
- 弹窗顶部 SHALL 显示标题"计时结果"
- 弹窗主体 SHALL 以列表形式展示每个分组的结果卡片，每张卡片包含：
  - 分组名称和计时时长
  - 排名标签（前三名显示对应名次徽章和颜色：第1名金色、第2名银色、第3名铜色）
  - 排名4+且达标时显示绿色"达标"标签
  - 排名4+且未达标时显示"继续加油"文字
  - 奖品图标和名称（如有绑定奖品）
- 列表中分组卡片 SHALL 按排名从高到低排列（第1名在前）；未进前三的分组排在后面

#### Scenario: 多分组汇总弹窗 - 混合排名展示
- **WHEN** 三个分组同时计时并全部停止，排名分别为第1名、第3名、第5名（但达标）
- **THEN** 汇总弹窗按排名顺序展示三张卡片：
  - 卡片1：第1名（金色徽章）+ 分组名 + 时长 + 奖品
  - 卡片2：第3名（铜色徽章）+ 分组名 + 时长 + 奖品
  - 卡片3：绿色"达标"标签 + 分组名 + 时长 + 奖品

#### Scenario: 多分组汇总弹窗 - 全部未进前三
- **WHEN** 两个分组同时计时并全部停止，排名均超过第3名
- **THEN** 汇总弹窗展示两张卡片，各自显示"继续加油"或"达标"状态

#### Scenario: 汇总弹窗庆祝动效
- **WHEN** 汇总弹窗中任意分组排名第1
- **THEN** 弹窗播放烟火动效
- **WHEN** 汇总弹窗中有分组排名第2-3但无第1名
- **THEN** 弹窗播放鲜花动效
- **WHEN** 汇总弹窗中无分组进入前三
- **THEN** 不播放庆祝动效

#### Scenario: 汇总弹窗关闭
- **WHEN** 用户点击汇总弹窗的"知道了"按钮或点击遮罩区域
- **THEN** 弹窗关闭，`rewardUiState` 设为 `Hidden`

### Requirement: 庆祝动效触发
系统 SHALL 根据排名触发不同的庆祝动效：
- 第1名：烟火动效
- 第2-3名：鲜花动效

#### Scenario: 第1名烟火动效
- **WHEN** 当前分组排名第1（用时最短）
- **THEN** 系统在弹窗显示时播放烟火粒子动画，持续约2秒

#### Scenario: 第2-3名鲜花动效
- **WHEN** 当前分组排名第2或第3
- **THEN** 系统在弹窗显示时播放鲜花粒子动画，持续约2秒

### Requirement: 动效性能约束
烟花/鲜花动画的粒子数量 SHALL 不超过 50 个，以确保中低端设备流畅运行。

### Requirement: 弹窗关闭
用户 SHALL 能通过点击弹窗外部或关闭按钮关闭奖励弹窗。

#### Scenario: 点击外部关闭
- **WHEN** 用户点击弹窗遮罩区域
- **THEN** 弹窗关闭，计时界面恢复

#### Scenario: 点击按钮关闭
- **WHEN** 用户点击弹窗中的"知道了"或关闭按钮
- **THEN** 弹窗关闭，计时界面恢复

### Requirement: 非前三名提示
对于排名不在前三的分组，系统 SHALL 显示鼓励文字"继续加油，再接再厉！"，不显示奖品和动效。

#### Scenario: 排名4及以后
- **WHEN** 当前分组排名为4或更低
- **THEN** 系统显示鼓励提示弹窗，不显示排名、奖品和动效