# lock-portrait-orientation: 锁定竖屏显示

## 概述

锁定 TinyTimer 应用为竖屏显示模式，禁止横屏显示。

## 问题与动机

当前应用支持横屏显示，但对于计时器应用来说，竖屏模式更为合适：
- 竖屏更符合手机使用习惯
- 计时器界面更适合垂直布局
- 避免用户误触旋转影响体验

## 预期结果

应用始终保持竖屏显示，无论设备如何旋转。

## 影响范围

- 修改 `AndroidManifest.xml`：设置 screenOrientation 属性
