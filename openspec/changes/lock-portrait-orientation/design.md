# lock-portrait-orientation: 设计文档

## 技术实现

在 AndroidManifest.xml 中为 MainActivity 设置 `screenOrientation` 属性为 `portrait`。

### 修改位置

`app/src/main/AndroidManifest.xml`

### 修改内容

在 `<activity>` 标签中添加属性：

```xml
<activity
    android:name=".ui.MainActivity"
    android:exported="true"
    android:screenOrientation="portrait"
    ...
>
```

## 验证方式

1. 旋转设备，确认应用始终保持竖屏
2. 在模拟器中使用 Ctrl+Left/Right 旋转屏幕，确认行为正确
