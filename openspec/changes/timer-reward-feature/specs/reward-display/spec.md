## ADDED Requirements

### Requirement: 奖励配置管理
系统 SHALL 通过 `PrizeDao.getPrizesByGroupId(groupId)` 查询分组绑定的奖品，复用现有 `PrizeEntity` 数据结构。奖品信息包含 name、imagePath、level(1/2/3)。

### Requirement: 奖品弹窗展示
系统 SHALL 在排名计算完成后，以半透明弹窗形式展示前三名的奖品信息，并明确显示名次所属分组。

#### Scenario: 显示奖品弹窗
- **WHEN** 排名前三计算完成
- **THEN** 系统显示包含排名、组名（如"第1名: 学习"）、时长和奖品信息的弹窗

#### Scenario: 奖品图标加载
- **WHEN** 弹窗需要显示奖品图标
- **THEN** 系统通过 `BitmapFactory.decodeFile(imagePath)` 从 `PrizeEntity.imagePath` 指定的本地文件路径加载图标，加载失败时显示默认占位图（礼物盒图标 `ic_reward_placeholder.xml`）

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