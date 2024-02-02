


import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.net.ConnectException
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit

fun main() {
    println("Author: Anthony")
    println("Version: 2.8.6")
    println("Date: 2024-02-1")
    script()
}

private fun script() {

    TimeUnit.SECONDS.sleep(1)
    pingFailReboot()
    val laptop = getLaptop()
    println("This laptop's IP address is "+laptop.ipAddress)
    println("This laptop's MAC address is "+laptop.macAddress)
    println("The host can be ping. So starting to connect to the host.")

    while(true){
        connectToServer()
        TimeUnit.SECONDS.sleep(4)
    }
}




private fun getLaptop(): Laptop {
    val ipAddress = InetAddress.getLocalHost().hostAddress
    val networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost())
    val macAddressByte = networkInterface.hardwareAddress

    val stringBuilder = StringBuilder()
    for (i in macAddressByte.indices) {
        stringBuilder.append(String.format("%02X%s", macAddressByte[i],
            if (i < macAddressByte.size - 1) "-" else ""))
    }
    val macAddress = stringBuilder.toString()
    val laptop = Laptop(ipAddress, macAddress)
    return laptop
}

private fun connectToServer() {
    runBlocking {
        val client = HttpClient {
            install(WebSockets)
        }
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
            client.close()
        } catch (_: ClosedReceiveChannelException) {
            println("ClosedReceiveChannelException happen.Restarting the script.")
            client.close()
        } catch (_: ConnectException) {
            println("ConnectException happen.Restarting the script.")
        } finally {
            client.close()
        }
    }
}

private fun pingFailReboot() {
    val ipAddress = "192.168.10.2"
    var isPingSuccess = false
    var falseCounter = 0
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