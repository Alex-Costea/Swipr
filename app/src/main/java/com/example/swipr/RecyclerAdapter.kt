package com.example.swipr
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

enum class Possibilities
{
    Unclicked, Empty, Bombed, Checked
}
class RecyclerAdapter(width: Int,var numBombs: Int): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>(){
    var logic: Logic = Logic(width,numBombs)
    private var width : Int = width
    var recSize:Int=0
    init
    {
        setHasStableIds(true)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        val inflatedView = parent.inflate(R.layout.items, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return logic.positionsList.size
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        holder.number=position
        if(recSize>0)
            holder.setWidth(recSize/width)
        holder.set(logic.textList[position],logic.positionsList[position])
    }

    override fun getItemId(position: Int): Long {
        return position.hashCode().toLong()
    }

    fun loadLogic(logic:Logic)
    {
        this.logic=logic
    }

    fun resetGame()
    {
        logic.reset()
        notifyDataSetChanged()
    }

    fun click(number: Int, longClick: Boolean)
    {
        logic.update(number,longClick)
        val updateItems=logic.updateItems
        for(i in updateItems)
        {
            notifyItemChanged(i)
        }
        //notifyDataSetChanged()
    }

    inner class ViewHolder (view: View) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {
        var pos : Possibilities = Possibilities.Empty
        var number:Int = 0
        private var myImageButton : ImageButton = view.findViewById(R.id.myImageButton)
        var myTextView : TextView = view.findViewById(R.id.myTextView)
        var view : View

        var colors : List<Int> = listOf(R.color.ripple_material_light,
            R.color.material_grey_50,
            R.color.error_color_material_light,
            R.color.material_deep_teal_500
            )

        init {
            myImageButton.setOnClickListener(this)
            myImageButton.setOnLongClickListener(this)
            myImageButton.isSoundEffectsEnabled=false
            this.view = view
        }

        fun setWidth(size: Int)
        {
            view.layoutParams= ViewGroup.LayoutParams(size,size)
        }

        fun set(text: String,myPos:Possibilities)
        {
            myTextView.text=text
            pos=myPos
            myImageButton.setBackgroundResource(colors[pos.ordinal])
        }
        override fun onClick(v: View?) {
            this@RecyclerAdapter.click(number,false)
        }

        override fun onLongClick(v: View?) : Boolean {
            this@RecyclerAdapter.click(number,true)
            return true
        }
    }
}

