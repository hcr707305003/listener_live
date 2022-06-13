package shiroi.top.bean

import com.google.gson.Gson
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.message.data.At
import shiroi.top.Plugin
import shiroi.top.service.DouYuService
import shiroi.top.traits.FileUtil
import shiroi.top.traits.JsonUtil
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

class DouYu {
    private var name:String = ""

    private var id:String = ""

    private var head:String = ""

    private var exists:Boolean = true//用户是否存在

    private var html:String = ""

    private var liveStatus:Boolean = false

    private var pattern = Pattern.CASE_INSENSITIVE

    private var headPattern = "<img class=\"DyImg-content is-normal?\" src=([\"'])?(?<src>[^'\"]+)[^>]*>"

    private var namePattern = "<div class=\"Title-anchorName\" title=([\"'])?(?<alt>[^'\"]+)[^>]*>"

//    private var liveStatusPattern = "window.station=['\"]online['\"]"
    private var liveStatusPattern = "show_status =\\s?1"

    private val douYuService = DouYuService.create

    /**
     * 创建结构以及初始化数据
     * @param id (斗鱼id)
     * @return DouYu
     */
    fun create(id: String): DouYu {
        //设置id
        setId(id.trim())
        //数据请求
        val h = douYuService.getId(id.trim()).execute().body().string()
        if(h.isNotEmpty()) {
            setHtml(h)
            initParam()
        }
        return this
    }

    private fun initParam() {
        //获取头像
        val h = Pattern.compile(this.headPattern, this.pattern).matcher(this.html)
        while (h.find()) {
            setHead(h.group("src"))
        }

        //获取名称
        val n = Pattern.compile(this.namePattern, this.pattern).matcher(this.html)
        while (n.find()) {
            setName(n.group("alt"))
        }

        //获取直播状态
        if(getName().isNotEmpty() && getHead().isNotEmpty()) {
            setLiveStatus(!Regex(this.liveStatusPattern).find(this.html)?.value.isNullOrEmpty())
        } else {
            this.exists = false
        }
    }

    fun getExists(): Boolean {
        return exists
    }

    fun getHtml(): String {
        return html
    }

    private fun setHtml(html:String) {
        this.html = html
    }

    fun getName(): String {
        return name
    }

    private fun setName(name:String) {
        this.name = name
    }

    fun getId(): String {
        return id
    }

    private fun setId(id:String) {
        this.id = id
    }

    fun getHead(): String {
        return head
    }

    private fun setHead(head:String) {
        this.head = head
    }

    fun getLiveStatus(): Boolean {
        return liveStatus
    }

    private fun setLiveStatus(liveStatus:Boolean) {
        this.liveStatus = liveStatus
    }

    override fun toString(): String {
        return "DouYu(name='$name', id='$id', head='$head', liveStatus=$liveStatus)"
    }

    class Config{
        //默认打开操作
        var isOpen:Boolean = true

        //开启的群聊(默认开始所有群聊)
        var group = arrayOf("*")

        //每隔多久(几分钟)监控一次(默认五分钟)
        var listen:Long = 5

        override fun toString(): String {
            return "DouYu(isOpen=$isOpen, group=${group.contentToString()}, listen=$listen)"
        }

        //初始化
        fun load() {
            val fileName = Plugin.configFolder.toString() + "\\" + "douyu.json"
            val fileUtil = FileUtil()
            if(!fileUtil.fileExists(fileName)) {
                FileUtil().saveFile(
                    fileName
                    , JsonUtil().toJsonParse(Gson().toJson(this))
                    ,true
                )
            }

            //注册命令行
            Command.register()

            //获取对象信息
            val configObject = Gson().fromJson(
                fileUtil.openFile(fileName),
                Config::class.java
            )

            //斗鱼消息订阅
            GlobalEventChannel.subscribeAlways<BotOnlineEvent> {
                if(configObject.isOpen) {
                    //自主回复
                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            Plugin.launch {
                                bot.groups.forEach {
                                    if(("*" in configObject.group) || (it.id.toString() in configObject.group)) {
                                        //保存的数据
                                        val fileName = Plugin.dataFolder.toString() + "\\data\\douyu\\${it.id}.json"
                                        //初始化文件
                                        if(FileUtil().fileExists(fileName)) {
                                            //主播列表
                                            var list = Gson().fromJson(FileUtil().openFile(fileName), DouYu().dataList::class.java)
                                            //获取所有keys
                                            var live = list.keys
                                            live.forEach { user ->
                                                //定义tmp文件
                                                var tmp = Plugin.dataFolder.toString() + "\\tmp\\douyu-${it.id}-${user}.tmp"
                                                //获取主播信息
                                                var body = DouYu().create(user)
                                                //直播中
                                                if(body.getLiveStatus()) {
                                                    if(!FileUtil().fileExists(tmp)) {
                                                        FileUtil().saveFile(tmp,user)
                                                        it.sendMessage("name:${body.getName()},id:${user},head=${body.getHead()}\n${DouYuService.base_url}${user}\n正在直播中~")
                                                    }
                                                } else {
                                                    FileUtil().deleteFile(tmp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }, Date(), configObject.listen * 60 * 1000)
                }
            }
        }
    }

    //配置data缓存
    class Data{
        //名称
        var name:String = ""

        //id
        var id:String = ""

        //头像
        var head:String = ""

        constructor(name: String, id: String, head: String) {
            this.name = name
            this.id = id
            this.head = head
        }
    }
    var dataList:HashMap<String,Data> = HashMap();

    //权限名 `/permission permit * shiroi.top.plugin:command.douyu:*`
    object Command: CompositeCommand(
        Plugin,"douyu",
        description = "斗鱼订阅指令"
    ) {

        @SubCommand("list", "列表")
        suspend fun UserCommandSender.list() {
            //所在群
            val group = subject
            //发送人
            val sender = user as Member
            if (group is Group) {
                //列表
                var strMessage = ""
                //保存的数据
                val fileName = Plugin.dataFolder.toString() + "\\data\\douyu\\${group.id}.json"
                //初始化文件
                if(FileUtil().fileExists(fileName)) {
                    var list = Gson().fromJson(FileUtil().openFile(fileName), DouYu().dataList::class.java)
                    //遍历数据
                    for (key in list.keys) {
                        strMessage += ("\n" + list[key].toString().trim('{').trim('}'))
                    }
                    group.sendMessage(At(sender.id) + (strMessage.ifEmpty { "暂无列表数据~" }))
                }
            }
        }

        @SubCommand("add", "添加")
        suspend fun CommandSender.add(id: String) {
            //所在群
            val group = subject
            //发送人
            val sender = user as Member
            if (group is Group) {
                //保存的数据
                val fileName = Plugin.dataFolder.toString() + "\\data\\douyu\\${group.id}.json"
                //获取主播信息
                val body = DouYu().create(id)
                //是否存在
                if(body.getExists()) {
                    var list = DouYu().dataList
                    //初始化文件
                    if(!FileUtil().fileExists(fileName)) {
                        list[id] = Data(body.getName(),body.getId(),body.getHead())
                        FileUtil().saveFile(
                            fileName
                            ,JsonUtil().toJsonParse(Gson().toJson(list))
                        )
                    } else {
                        list = Gson().fromJson(FileUtil().openFile(fileName), DouYu().dataList::class.java)
                        list[id] = Data(body.getName(),body.getId(),body.getHead())
                        FileUtil().saveFile(
                            fileName
                            ,JsonUtil().toJsonParse(Gson().toJson(list))
                            ,true
                        )
                    }
                    group.sendMessage(At(sender.id) + "\n添加成功~")
                } else {
                    group.sendMessage(At(sender.id) + "\n用户不存在，添加失败~")
                }
            }
        }

        @SubCommand("del", "删除")
        suspend fun CommandSender.del(id: String) {
            //所在群
            val group = subject
            //发送人
            val sender = user as Member
            if (group is Group) {
                //保存的数据
                val fileName = Plugin.dataFolder.toString() + "\\data\\douyu\\${group.id}.json"
                //文件是否存在
                if(FileUtil().fileExists(fileName)) {
                    var list = Gson().fromJson(FileUtil().openFile(fileName), DouYu().dataList::class.java)
                    list.remove(id)
                    FileUtil().saveFile(
                        fileName
                        ,JsonUtil().toJsonParse(Gson().toJson(list))
                        ,true
                    )
                }
                group.sendMessage(At(sender.id) + "\n删除成功~")
            }
        }
    }
}