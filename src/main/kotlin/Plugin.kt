package shiroi.top

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import shiroi.top.bean.DouYu
import shiroi.top.bean.HuYa
object Plugin : KotlinPlugin(
    JvmPluginDescription(
        id = "shiroi.top.plugin",
        version = "1.0",
        name = "listenerLive"
    ) {
        author("shiroi")
    }
) {
    override fun onEnable() {
        DouYu.Config().load()
        HuYa.Config().load()
        logger.info{"配置加载完毕"};
    }
}