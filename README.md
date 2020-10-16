# HttpUtilDemo
Http请求工具类 KT

# 使用
` implementation 'com.github.IAmWilling:HttpUtilDemo:1.0.0' `

# 说明
 - 此库包含了okhttp4.x以及gson 2.8版本
 - 需保证引用不会引起依赖冲突，引入此库只会不需要在额外引入okhttp库以及gson
 - 其他还在更新中，不断完善
 
# 使用
- get 只有一种 普通get请求 按照规定传参
- post 两种(1.json post 2.表单提交post)
- postFile 文件上传
- UploadProgress 文件上传进度监听 返回当前上传进度 0 ~ 1

# 特别说明
这些请求第一个参数都是lifecycle，要和特定的Activity或者Fragment的生命周期绑定，
这样，当一个Activity或者Fragment销毁的时候，会取消当前绑定的界面的一系列请求，防止空指针异常的问题
