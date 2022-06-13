package shiroi.top.traits

import java.io.*

class FileUtil {

    //保存文本文件
    fun saveFile(fileName: String, txt: String, isCover: Boolean = false) {
        try {
            //文件夹是否存在
            createPath(fileName.substringBeforeLast("\\"))
            val fos = FileOutputStream(fileName)
            // 构建FileOutputStream对象,文件不存在会自动新建
            val writer = OutputStreamWriter(fos, "UTF-8")
            //追加或者覆盖的操作
            if(isCover) writer.write(txt) else writer.append(txt)
            writer.close()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //读取文本文件
    fun openFile(path: String): String? {
        var readStr: String? = ""
        try {
            val fis = FileInputStream(path)
            val b = ByteArray(fis.available())
            fis.read(b)
            readStr = String(b)
            fis.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return readStr
    }

    /**
     * 文件夹创建
     * @param path
     * @return boolean
     */
    fun createPath(path: String): Boolean {
        val file = File(path)
        return if(!file.exists()) {
            file.mkdirs()
        } else {
            false
        }
    }

    fun fileExists(fileName: String): Boolean {
        return File(fileName).exists()
    }

    fun deleteFile(fileName: String): Boolean {
        val file = File(fileName)
        return if(file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}