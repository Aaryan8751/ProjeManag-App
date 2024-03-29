package com.example.projemanag.activities

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.User
import kotlinx.android.synthetic.main.activity_my_profile.*
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.projemanag.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {


    private var mSelectedImageFileUri : Uri? = null
    private var mProfileImageURL : String? =null
    private lateinit var mUserDetails : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()

        FirestoreClass().loadUserData(this)

        iv_profile_user_image.setOnClickListener{

            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this@MyProfileActivity)
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE)
            }

        }

        btn_update.setOnClickListener {
            if(mSelectedImageFileUri!=null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this@MyProfileActivity)
            }
        }else{
            Toast.makeText(this,
                "Oops you just denied the permission for storage. You can also allow it from settings",
                Toast.LENGTH_SHORT).show()
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode== Constants.PICK_IMAGE_REQUEST_CODE && data!!.data!=null){
                mSelectedImageFileUri = data.data
                try{
                    Glide
                        .with(this@MyProfileActivity)
                        .load(mSelectedImageFileUri)
                        .circleCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(iv_profile_user_image)

                }catch (e:IOException){
                    e.printStackTrace()
                }

            }
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_my_profile_activity)
        if(supportActionBar!=null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            supportActionBar!!.title = resources.getString(R.string.my_profile)
        }
        toolbar_my_profile_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    fun setUserDataInUI(user: User){
        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .circleCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_profile_user_image)

        et_name.setText(user.name)
        et_email.setText(user.email)
        if(user.mobile!=0L){
            et_mobile.setText(user.mobile.toString())
        }

        mUserDetails = user
    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String,Any>()
        var anyChangesMade = false
        if(mProfileImageURL!=null && mProfileImageURL!=mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL!!
            anyChangesMade = true
        }

        if(et_name.text.toString()!=mUserDetails.name){
            userHashMap[Constants.NAME] = et_name.text.toString()
            anyChangesMade = true
        }

        if(et_mobile.text.toString()!=mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = et_mobile.text.toString().toLong()
            anyChangesMade = true
        }
        if(anyChangesMade){
            FirestoreClass().updateUserProfileData(this@MyProfileActivity,userHashMap)
        }

    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if(mSelectedImageFileUri!=null){

            val sRef :StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "USER_IMAGE"+System.currentTimeMillis()
                            +"."+Constants.getFileExtension(this@MyProfileActivity,mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener {
                    taskSnapshot->
                        Log.i(
                            "Firebase Image URL",
                            taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                        )

                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri->
                        Log.i("Downloadable ",uri.toString())
                        mProfileImageURL=uri.toString()

                        updateUserProfileData()
                    }
                }
                .addOnFailureListener{
                    exception->
                    Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    hideProgressDialog()
                }
        }
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

}