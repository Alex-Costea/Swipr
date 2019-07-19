package com.example.swipr

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var fullWidth : Int=0
    private val defaultBombs=10
    private val defaultWidth=12
    private var logic : Logic? = null

    /**
     * Redoes the Recycler View from scratch and resets the game.
     * @param size The width and heigth of the grid
     * @param bombsNr The number of bombs
     */
    private fun redoRecyclerView(size:Int, bombsNr:Int)
    {
        var adapter=RecyclerAdapter(size,bombsNr)
        recyclerView.layoutManager = GridLayoutManager(this, size)
        recyclerView.layoutParams.width=fullWidth/size*size
        recyclerView.layoutParams.height=fullWidth/size*size
        adapter.recSize=fullWidth
        adapter.resetGame()
        val myLogic=logic
        if(myLogic!=null)
        {
            adapter.loadLogic(myLogic)
            logic=Logic(myLogic.width,myLogic.numBombs)
        }
        recyclerView.adapter=adapter
    }

    /**
     * Runs when a new game is started
     */
    override fun onClick(v: View?) {
        var width = 0
        if(editText2.text.toString()!="")
            width=editText2.text.toString().toInt()
        width=max(4,min(20,width))
        var bombsNr=0
        if(editText.text.toString()!="")
            bombsNr=editText.text.toString().toInt()
        bombsNr=min(width*width-9,max(1,bombsNr))
        if(fullWidth==0)
            fullWidth=recyclerView.width
        redoRecyclerView(width,bombsNr)

    }

    /**
     * Initiates the game when first opened
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText.setText(defaultBombs.toString())
        editText2.setText(defaultWidth.toString())
        supportActionBar?.hide()
        newGameButton.setOnClickListener(this)
        recyclerView.post { newGameButton.performClick() }
        if(savedInstanceState!=null)
        {
            logic = savedInstanceState.getParcelable<Logic>("Logic")
        }

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable("Logic",(recyclerView.adapter as RecyclerAdapter).logic)
    }
}
