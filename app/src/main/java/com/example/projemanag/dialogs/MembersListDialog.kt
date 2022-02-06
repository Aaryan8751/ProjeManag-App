package com.example.projemanag.dialogs

import android.app.Dialog
import android.content.Context
import android.icu.text.CaseMap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanag.R
import com.example.projemanag.adapters.MemberListItemAdapter
import com.example.projemanag.models.User
import kotlinx.android.synthetic.main.dialog_list.view.*

abstract class MembersListDialog(
    context: Context,
    private var list : ArrayList<User>,
    private var title: String = "",
): Dialog(context){

    private var adapter : MemberListItemAdapter? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(
            R.layout.dialog_list,
            null)
        setContentView(view)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view:View){
        view.tvTitle.text = title

        if(list.size>0){
            view.rvList.layoutManager = LinearLayoutManager(context)
            adapter = MemberListItemAdapter(context,list)
            view.rvList.adapter = adapter

            adapter!!.setOnClickListener(object :MemberListItemAdapter.OnClickListener{
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user,action)
                }

            })
        }
    }

    protected abstract fun onItemSelected(user:User,action:String)
}