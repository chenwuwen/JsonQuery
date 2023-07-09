![Alt](logo.png)

JsonQuery是一个使用SQL方式来查询本地json文件的一个工具,支持自定义函数,底层采用Calcite作为查询引擎。

JsonQuery分为两个模块：
ui: 负责客户端界面
sql：负责sql执行

ui模块主要使用javaFx

sql模块则包含了自定义表,自定义Schema,及自定义函数

使用JsonQuery可以添加Schema,通过指定Json文件所在路径,即可创建Schema,路径下的json文件名,即时table,
工具可以自行推断table中的字段信息,如果推断信息有误,用户也可以自定修改字段信息。