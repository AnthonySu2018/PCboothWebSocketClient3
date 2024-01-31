import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.net.InetAddress
import java.util.concurrent.TimeUnit

fun main() {
    println("Author: Anthony")
    println("Version: 2.4")
    println("Date: 2024-01-31")
    println("Please don't close this windows.")
    AnthonyScript()

}

private fun AnthonyScript() {
    val client = HttpClient {
        install(WebSockets)
    }


    pingFailReboot()
    println("The host can be ping. So starting to connect to the host.")
    connectToServer(client)
}

private fun connectToServer(client: HttpClient) {
    runBlocking {
        try {
            client.webSocket(method = HttpMethod.Get, host = "192.168.10.2", port = 50000, path = "/chat") {
                println("Connected to the host.")
                while (true) {
                    val othersMessage = incoming.receive() as? Frame.Text ?: continue
                    println(othersMessage.readText())
                    val task = othersMessage.readText()
                    when (task) {
                        "reboot" -> {
                            reboot()
                        }
                        "shutdown" -> {
                            shutdown()
                        }
                    }
                    /*                val myMessage = readlnOrNull()
                                    if(myMessage != null) {
                                        send(myMessage)
                                    }*/
                }
            }
        } catch (_: IOException) {
            println("IOException happen. Restarting the script.")
            TimeUnit.SECONDS.sleep(3)
            AnthonyScript()
        } catch (_: ClosedReceiveChannelException) {
            println("ClosedReceiveChannelException happen.Restarting the script.")
            TimeUnit.SECONDS.sleep(3)
            AnthonyScript()
        }
    }
}

private fun pingFailReboot(ipAddress: String = "192.168.10.2",
                            isPingSuccess: Boolean = false,
                            falseCounter: Int  = 0) {

    var isPingSuccess = isPingSuccess
    var falseCounter = falseCounter
    do {
        ping(ipAddress)
        isPingSuccess = ping(ipAddress)
        println("Ping $ipAddress,and $isPingSuccess")
        TimeUnit.SECONDS.sleep(4)
        falseCounter++
        println("failed to connect to host,the $falseCounter time")
        if (falseCounter > 10) {
            reboot()
        }
    } while (!isPingSuccess)
}


private fun shutdown() {
    try {
        val command = "shutdown.exe -p" // -p 表示关机
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}

private fun reboot() {
    try {
        val command = "shutdown.exe -r -t 0 " // -r 表示重启，-t 0 表示无延迟立即重启
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}

fun ping(ipAddress: String): Boolean {
    return try {
        val inet = InetAddress.getByName(ipAddress)
        inet.isReachable(1000) // 设置超时时间为1秒
    } catch (ex: Exception) {
        false
    }
}