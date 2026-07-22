package com.example.accessories_app.tryon.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileOutputStream

class TryOnImageRepository(
    private val context: Context
) {

    fun copyAndDecode(
        uri: Uri
    ): Bitmap {
        val file = copyToInternalStorage(uri)
        return decode(file)
    }

    private fun copyToInternalStorage(
        uri: Uri
    ): File {
        val directory = File(
            context.filesDir,
            DIRECTORY_NAME
        )

        check(
            directory.exists() || directory.mkdirs()
        ) {
            "Could not create try-on image directory"
        }

        val extension = when (
            context.contentResolver.getType(uri)
        ) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }

        val file = File(
            directory,
            "user_image_${System.currentTimeMillis()}.$extension"
        )

        val inputStream =
            requireNotNull(
                context.contentResolver.openInputStream(uri)
            ) {
                "Could not open selected image"
            }

        inputStream.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file
    }

    private fun decode(
        file: File
    ): Bitmap {
        return if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        ) {
            val source =
                ImageDecoder.createSource(file)

            ImageDecoder.decodeBitmap(
                source
            ) { decoder, _, _ ->
                decoder.allocator =
                    ImageDecoder.ALLOCATOR_SOFTWARE

                decoder.isMutableRequired = true
            }
        } else {
            requireNotNull(
                BitmapFactory.decodeFile(
                    file.absolutePath
                )
            ) {
                "Could not decode selected image"
            }
        }
    }

    private companion object {
        const val DIRECTORY_NAME = "try_on_uploads"
    }
}
