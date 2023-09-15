package com.applligent.formdatarequest

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.applligent.formdatarequest.databinding.ActivityMainBinding
import com.applligent.formdatarequest.databinding.BottomSelectImageDialogBinding
import com.applligent.formdatarequest.databinding.ItemImagesLayoutBinding
import com.applligent.formdatarequest.network.ApiClient
import com.applligent.formdatarequest.network.ApiInterface
import com.applligent.formdatarequest.network.Repository
import com.applligent.koindi.utils.Status
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val binding : ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    var imageString = ""
    private var singleFileImage : File? = null
    private val arrayListFileImages = ArrayList<File>()

    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(
                Repository(
                    ApiClient().getClient()!!.create(ApiInterface::class.java)
                )
            )
        )[MainViewModel::class.java]
        setOnClickListener()
    }

    private var isImageSelectFrom = false
    private fun setOnClickListener() {
        //for single photo
        /*binding.llUploadImg.setOnClickListener {
            selectOptionToGetImage()
            isImageSelectFrom = true
        }*/
        //for multiple photos
        binding.llUploadImg.setOnClickListener {
            selectOptionToGetMultipleImage()
             isImageSelectFrom = false
        }

        binding.btnSave.setOnClickListener {
            val multipartBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("subcategory_id", "")
                .addFormDataPart("type", "")
                .addFormDataPart("name", "")
                .addFormDataPart("localname", "")
                .addFormDataPart("unit", "")
                .addFormDataPart("pack_qty", "")
                .addFormDataPart("shelf_life", "")
                .addFormDataPart("weight","")
                .addFormDataPart("price", "")
                .addFormDataPart("quantity", "")
                .addFormDataPart("offer", "")
                .addFormDataPart("ingredients", "")
                .addFormDataPart("description", binding.etDesc.text.toString())

            for (imageFile in arrayListFileImages) {
                //val mediaType = "image/*".toMediaTypeOrNull()
                //val requestBody = imageFile.asRequestBody(mediaType)
                val requestBody = imageFile.asRequestBody(MultipartBody.FORM)
                multipartBuilder.addFormDataPart("images[]", imageFile.name, requestBody)
            }
            val requestBody: RequestBody = multipartBuilder.build()
            viewModel.addProduct(requestBody,"token")
            setObserver()

            //form data request using RequestBody
            /*val requestBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("category_id", categoryId.toString())
                .addFormDataPart("subcategory_id", subCategoryId.toString())
                .addFormDataPart("type", productType)
                .addFormDataPart("name", productName)
                .addFormDataPart("localname", productLocalName)
                .addFormDataPart("unit", productUnit)
                .addFormDataPart("pack_qty", productPackQuantity.toString())
                .addFormDataPart("shelf_life", productShelfLife)
                .addFormDataPart("weight", productWeight.toString())
                .addFormDataPart("price", productPrice.toString())
                .addFormDataPart("quantity", productQuantity.toString())
                .addFormDataPart("offer", productOffer.toString())
                .addFormDataPart("ingredients", productIngredient)
                .addFormDataPart("description", binding.etAddProductDesc.text.toString())
                .addFormDataPart("display_image", displayImage?.name, RequestBody.create(MultipartBody.FORM, displayImage!!))
                .addFormDataPart("images[]", multiImg1?.name, RequestBody.create(MultipartBody.FORM, multiImg1!!))
                .addFormDataPart("images[]", multiImg2?.name, RequestBody.create(MultipartBody.FORM, multiImg2!!))
                .addFormDataPart("images[]", multiImg3?.name, RequestBody.create(MultipartBody.FORM, multiImg3!!))
                .addFormDataPart("images[]", multiImg4?.name, RequestBody.create(MultipartBody.FORM, multiImg4!!))
                .addFormDataPart("images[]", multiImg5?.name, RequestBody.create(MultipartBody.FORM, multiImg5!!))
                .addFormDataPart("video[]", videoSingleArray!!)
                .build()
            viewModel.addProduct(requestBody,requireActivity().getToken())*/
        }
    }

    private fun setObserver() {
        viewModel.users.observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    //it.data?.success
                    //binding.progressBar.visibility = View.GONE
                    //it.data?.let { users -> renderList(users) }
                    //binding.recyclerView.visibility = View.VISIBLE
                }
                Status.LOADING -> {
                    //binding.progressBar.visibility = View.VISIBLE
                    //binding.recyclerView.visibility = View.GONE
                }
                Status.ERROR -> {
                    //Handle Error
                    //binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun selectOptionToGetImage() {
        val dialogBinding: BottomSelectImageDialogBinding =
            BottomSelectImageDialogBinding.inflate(layoutInflater, null, false)
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(dialogBinding.root)
        bottomSheetDialog.show()
        dialogBinding.tvDialogSelectImgTitle.text = "Add photo"
        dialogBinding.tvDialogSelectImgDes.text = "Take photo from gallery or camera"
        dialogBinding.dialogSelectImgClose.setOnClickListener { bottomSheetDialog.dismiss() }
        dialogBinding.dialogSelectImgGalleyBtn.setOnClickListener {
            takeImageFromGallery.launch("image/*")
            bottomSheetDialog.dismiss()
        }
        dialogBinding.dialogSelectImgCameraBtn.setOnClickListener {
            takeImageFromCamera.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            bottomSheetDialog.dismiss()
        }
    }

    @SuppressLint("IntentReset", "SetTextI18n")
    private fun selectOptionToGetMultipleImage() {
        val dialogBinding: BottomSelectImageDialogBinding =
            BottomSelectImageDialogBinding.inflate(layoutInflater, null, false)
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(dialogBinding.root)
        bottomSheetDialog.show()
        dialogBinding.tvDialogSelectImgTitle.text = "Add photo"
        dialogBinding.tvDialogSelectImgDes.text = "Take photo from gallery or camera"
        dialogBinding.dialogSelectImgClose.setOnClickListener { bottomSheetDialog.dismiss() }
        dialogBinding.dialogSelectImgGalleyBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), 100)
            }else{
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                startActivityForResult(intent, PICK_IMAGE_MULTIPLE)
                bottomSheetDialog.dismiss()
            }
        }
        dialogBinding.dialogSelectImgCameraBtn.setOnClickListener {
            takeImageFromCamera.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            bottomSheetDialog.dismiss()
        }
    }

    //form gallery
    private var takeImageFromGallery = registerForActivityResult<String, Uri>(
        ActivityResultContracts.GetContent()
    ) { result: Uri? ->
        if (result != null){
            val imageUri: Uri = result
            //val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
            singleFileImage = convertUriToFile(imageUri)
            var imageBitmap: Bitmap? = null
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, result)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            assert(imageBitmap != null)
            //imageString = convertBitmapToString(imageBitmap)
            addImageToLinearLayout(result)
        }
    }

    //from camera
    private var takeImageFromCamera = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val bundle = result.data!!.extras
            val bitmap = bundle!!["data"] as Bitmap?
            //imageString = convertBitmapToString(bitmap)
            val imageUri = convertBitmapToUri(this,bitmap!!)
            singleFileImage = convertUriToFile(imageUri!!)
            addImageToLinearLayout(imageUri)
        }
    }

    //use for video and multiple images
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // When Images are picked
        if (!isImageSelectFrom){
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == Activity.RESULT_OK) {
                val selectedImages = mutableListOf<Uri>()
                val clipData = data?.clipData
                if (clipData != null) {
                    val selectedCount = clipData.itemCount
                    if (selectedCount > MAX_IMAGE_COUNT) {
                        Toast.makeText(this, "You Select only 5 Images", Toast.LENGTH_LONG).show()
                        return
                    }
                    for (i in 0 until selectedCount) {
                        val imageUri = clipData.getItemAt(i).uri
                        //al bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        addImageToLinearLayout(imageUri)
                        selectedImages.add(imageUri)
                    }
                } else {
                    val imageUri = data?.data
                    if (imageUri != null) {
                        selectedImages.add(imageUri)
                    }
                }
            }else{
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addImageToLinearLayout(imgUri: Uri) {
        val file = convertUriToFile(imgUri)
        arrayListFileImages.add(file)
         val bindingLayout: ItemImagesLayoutBinding =
             ItemImagesLayoutBinding.inflate(layoutInflater, null, false)
         bindingLayout.ivFeedbackPhotos.setImageURI(imgUri)
         binding.emptyImagesLinearLayout.addView(bindingLayout.root)
         bindingLayout.tvRequestCode.text = binding.emptyImagesLinearLayout.childCount.toString()
         bindingLayout.ivItemAddPhotoDelete.setOnClickListener {
             binding.emptyImagesLinearLayout.removeView(bindingLayout.root)
             val position = bindingLayout.tvRequestCode.text.toString().toInt()
             //array
             arrayListFileImages.removeAt(position)
         }
    }


    private var count = 0
    @SuppressLint("Recycle")
    private fun convertUriToFile(imgUri: Uri): File{
        count += 1
        val filesDir = filesDir
        val file = File(filesDir,"$count"+"image.png")
        val inputStream = contentResolver.openInputStream(imgUri)
        val outputStream = FileOutputStream(file)
        inputStream!!.copyTo(outputStream)
        return file
    }

    //bitmap to uri
    private fun convertBitmapToUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }

    //bitmap to url
    /*private fun convertBitmapToUrl(bitmap: Bitmap) {
       count =+ 1
       val storage = FirebaseStorage.getInstance()
       val storageRef: StorageReference = storage.reference
       val fileName = UUID.randomUUID().toString()
       val imageRef: StorageReference = storageRef.child("images$count/$fileName.png")
       val baos = ByteArrayOutputStream()
       bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
       val data: ByteArray = baos.toByteArray()
       val uploadTask = imageRef.putBytes(data)
       uploadTask.continueWithTask { task ->
           if (!task.isSuccessful) {
               task.exception?.let { throw it }
           }
           imageRef.downloadUrl
       }.addOnCompleteListener { task ->
           if (task.isSuccessful) {
               val downloadUri = task.result
               val imageUrl = downloadUri.toString()

           } else {
               toast("Something went wrong")
           }
       }
   }*/
    //bitmap to Base64 string
    private fun convertBitmapToString(bitmap: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    /*if u want convert url to bitmap
        Glide.with(this@EditProductActivity)
            .asBitmap()
            .load(displayImage!!)
            .into(object : SimpleTarget<Bitmap?>() {
                override fun onResourceReady(
                    bitmap: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap?>?
                ) {
                    binding.ivEditProductDisplayImage.setImageBitmap(bitmap)
                    val imageUri = getImageUri(this@EditProductActivity, bitmap)
                    displayImageFile = convertUriToFile(imageUri!!)
                }
            })*/

    companion object {
        private const val PICK_IMAGE_MULTIPLE = 1
        private const val MAX_IMAGE_COUNT = 5
    }
}