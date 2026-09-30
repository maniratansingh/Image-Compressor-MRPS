import java.io.File
fun main() {
    val path = "content://in.mrps.imagecompressor.fileprovider/cache_path/COMP_123.jpg"
    val parts = path.split("/")
    println(parts.last())
}
main()
