package com.example.projemanag.activities

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.projemanag.R
import kotlinx.android.synthetic.main.activity_create_board.*
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private var mSelectedImageFileUri : Uri? =null

    private lateinit var mUserName : String

    private var mBoardImageURL :String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)
        setupActionBar()

        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME) as String
        }

        iv_board_image.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this@CreateBoardActivity)
            }
            else{
                ActivityCompat.requestPermissions(this@CreateBoardActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),Constants.READ_STORAGE_PERMISSION_CODE)
            }
        }

        btn_create.setOnClickListener {
            if(mSelectedImageFileUri!=null){
                uploadBoardImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }

    }

    private fun createBoard(){
        val assignedUsersArrayList : ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserId())

        var board : Board = Board(
            et_board_name.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsersArrayList
        )

        FirestoreClass().createBoard(this,board)

    }

    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

            val sRef : StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "BOARD_IMAGE"+System.currentTimeMillis()
                            +"."+Constants.getFileExtension(this@CreateBoardActivity,mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener {
                        taskSnapshot->
                    Log.i(
                        "Board Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )

                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                            uri->
                        Log.i("Downloadable ",uri.toString())
                        mBoardImageURL=uri.toString()

                        createBoard()
                    }
                }
                .addOnFailureListener{
                        exception->
                    Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    hideProgressDialog()
                }
        }



    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this@CreateBoardActivity)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK){
            if(requestCode==Constants.PICK_IMAGE_REQUEST_CODE && data!!.data!=null){
                mSelectedImageFileUri = data.data
                try{
                    Glide
                        .with(this)
                        .load(mSelectedImageFileUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_board_place_holder)
                        .into(iv_board_image)

                }catch (e: IOException){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_create_board_activity)
        if(supportActionBar!=null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            supportActionBar!!.title = resources.getString(R.string.create_board_title)
        }

        toolbar_create_board_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}