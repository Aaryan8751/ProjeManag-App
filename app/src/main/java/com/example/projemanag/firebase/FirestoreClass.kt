package com.example.projemanag.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projemanag.activities.*
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.lang.reflect.Member

class FirestoreClass {
    private var mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity:SignUpActivity,userInfo: User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener{
                e->
            Log.e(activity.javaClass.simpleName,"Error writing document",e)
            }
    }

    fun createBoard(activity: CreateBoardActivity,board:Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName,"Board created successfully")
                Toast.makeText(activity, "Board Created Successfully.", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener {
                    e->
                Log.e(activity.javaClass.simpleName,"Error writing document",e)
            }
    }

    fun getBoardsList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserId())
            .get()
            .addOnSuccessListener {
                document->
                Log.i(activity.javaClass.simpleName,document.documents.toString())
                val boardList:ArrayList<Board> = ArrayList()

                for(i in document.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                activity.populateBoardsListToUI(boardList)
            }
            .addOnFailureListener{
                e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while getting the boards",e)
            }
    }

    fun addUpdateTaskList(activity: Activity,board:Board){
        val taskListHashMap = HashMap<String,Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName,"TaskList updated successfully")
                if(activity is TaskListActivity){
                    activity.addUpdateTaskListSuccess()
                }
                else if(activity is CardDetailsActivity){
                    activity.addUpdateTaskListSuccess()
                }
            }
            .addOnFailureListener{e->
                if(activity is TaskListActivity){
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName,"Error while creating a board",e)
                }
                else if(activity is CardDetailsActivity){
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName,"Error while creating a board",e)
                }
            }
    }

    
    fun updateUserProfileData(activity: MyProfileActivity,userHashMap:HashMap<String,Any>){
        
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener { 
                Log.i(activity.javaClass.simpleName,"Profile Data updated successfully")
                Toast.makeText(activity, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener{
                e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating a board",e)
                Toast.makeText(activity, "Profile updated Error", Toast.LENGTH_SHORT).show()
            }
    }

    fun loadUserData(activity: Activity,readBoardList:Boolean = false){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)
                if(loggedInUser!=null){
                    when(activity){
                        is SignInActivity ->{
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity ->{
                            activity.updateNavigationUserDetails(loggedInUser,readBoardList)
                        }
                        is MyProfileActivity ->{
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }

            }
            .addOnFailureListener{
                e->
                when(activity){
                    is SignInActivity->{
                        activity.hideProgressDialog()
                    }
                    is MainActivity->{
                        activity.hideProgressDialog()
                    }
                }
            Log.e(activity.javaClass.simpleName,"Error writing document",e)
            }
    }

     fun getCurrentUserId():String{

        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID =""
        if(currentUser!=null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getBoardDetails(activity : TaskListActivity, documentId: String) {

        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document->
                Log.i(activity.javaClass.simpleName,document.toString())
                val board = document.toObject(Board::class.java)
                board!!.documentId = document.id
                activity.boardDetails(board)

            }
            .addOnFailureListener{
                    e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while getting the boards",e)
            }

    }

    fun getAssignedMembersListDetails(activity: Activity,assignedTo:ArrayList<String>){
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID,assignedTo)
            .get()
            .addOnSuccessListener {
                document ->
                Log.e(activity.javaClass.simpleName,document.documents.toString())

                val userList : ArrayList<User> = ArrayList()

                for(i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    userList.add(user)
                }
                if(activity is MembersActivity){
                    activity.setupMembersList(userList)
                }
                else if(activity is TaskListActivity){
                    activity.boardMembersDetailsList(userList)
                }
            }
            .addOnFailureListener { e->
                if(activity is MembersActivity){
                    activity.hideProgressDialog()
                }
                else if (activity is TaskListActivity){
                    activity.hideProgressDialog()
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board",
                    e
                )
            }
    }

    fun getMemberDetails(activity: MembersActivity,email:String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL,email)
            .get()
            .addOnSuccessListener {
                document->
                if(document.documents.size>0){
                    val user = document.documents[0].toObject(User::class.java)
                    if (user != null) {
                        activity.memberDetails(user)
                    }else{
                        activity.hideProgressDialog()
                        activity.showErrorSnackBar("No such member found")
                    }
                }
            }
            .addOnFailureListener {e->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting a user details",
                    e
                )
            }
    }

    fun assignMemberToBoard(activity: MembersActivity,board:Board,user:User){

        val assignedToHashMap = HashMap<String,Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener {
                e->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while assigning a member to a board",
                    e
                )
            }

    }

}