# ZIP打包插件
中通快递持续集成系统底层用到了Jenkins，目前持续集成系统支持Java、DotNet、前端静态项目、前端动态项目、Go项目的build，对于前端静态项目和前端动态项目，build之后并不是一个二进制文件，而是一个目录，所以我们需要对这个目录打成一个zip包，上传到制品库中。


# 生成插件
```
mvn clean package -DskipTests
```
最后会打包成一个hpi文件，这是jenkins的插件文件

# 安装插件
我们在jenkins的系统管理->管理插件->高级-上传插件中上传我们生成的hpi文件，上传成功后等待jenkins重启。

# 在pipeline中使用插件
```
stage('打包') {
    steps {
        script{
            file = zipFile source:"buildout",excludes:""
        }
    }
}
```
source：要打包的目录名称
excludes：文件排除规则

此插件只能用在pipeline风格的项目中