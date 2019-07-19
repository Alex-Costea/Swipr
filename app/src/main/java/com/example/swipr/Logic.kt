package com.example.swipr

import android.os.Parcel
import android.os.Parcelable
import kotlin.random.Random

class Logic (
    var width: Int,
    var numBombs: Int,
    private var gameEnded: Boolean = false,
    private var size: Int = width * width,
    var positionsList: MutableList<Possibilities> = mutableListOf(),
    var textList: MutableList<String> = mutableListOf(),
    private var bombs: MutableList<Int> = mutableListOf(),
    private var initGuessed: Boolean = false,
    private var invalidatedItems: MutableList<Int> = mutableListOf()
) : Parcelable {

    private val mooreNeighborhood =
        mutableListOf(Pair(-1,-1),Pair(-1,0),Pair(-1,1),Pair(0,-1),Pair(0,1),Pair(1,-1),Pair(1,0),Pair(1,1))

    var updateItems:MutableList<Int>
    get() {val y=invalidatedItems;invalidatedItems = mutableListOf(); return y;}
    set(value){invalidatedItems=value}

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.createStringArray().map{Possibilities.valueOf(it)}.toMutableList(),
        parcel.createStringArray().toMutableList(),
        parcel.createIntArray().toMutableList(),
        parcel.readByte() != 0.toByte(),
        parcel.createIntArray().toMutableList()
    )

    init {
        if(positionsList.isEmpty())
            for(i in 1..size)
            {
                positionsList.add(Possibilities.Unclicked)
                textList.add("")
            }
    }

    /**
     * Resets the game
     */
    fun reset()
    {
        gameEnded=false
        positionsList= mutableListOf()
        textList= mutableListOf()
        for(i in 1..size)
        {
            positionsList.add(Possibilities.Unclicked)
            textList.add("")
        }
        bombs=mutableListOf()
        initGuessed=false
        invalidatedItems= mutableListOf()
    }

    /**
     * Initiates the location of the bombs
     */
    private fun initBombs(nrBombs:Int,initGuess:Int)
    {
        bombs=mutableListOf(initGuess)
        val initGuessX=numberToPos(initGuess).first
        val initGuessY=numberToPos(initGuess).second
        var bombsCleared=1
        for(x in mooreNeighborhood)
        {
            val i=x.first
            val j=x.second
            if(isValid(initGuessX+i,initGuessY+j))
            {
                val currentBomb = posToNumber(initGuessX + i, initGuessY + j)
                bombs.add(currentBomb)
                bombsCleared++
            }
        }
        for(i in 0 until nrBombs)
        {
            val bombLocation=Random.nextInt(0,size-i-bombsCleared)
            var j=0
            for(j2 in 0 until bombs.size)
            {
                if(bombs[j2]>bombLocation+j)
                {
                    break
                }
                j++
            }
            bombs.add(j,bombLocation+j)
        }
        bombs.remove(initGuess)
        for(x in mooreNeighborhood)
        {
            val i=x.first
            val j=x.second
            if(isValid(initGuessX+i,initGuessY+j))
            {
                val currentBomb = posToNumber(initGuessX + i, initGuessY + j)
                bombs.remove(currentBomb)
            }
        }
    }

    /**
     * Transforms a number to the XY coordonates on the grid
     */
    private fun numberToPos(number:Int) = Pair((number)/width+1,(number)%width+1)

    /**
     * Returns true if the XY coordonates are valid on the grid, false otherwise
     */
    private fun isValid(x:Int,y:Int) : Boolean
    {
        if(x<1) return false
        if(y<1) return false
        if(x>width) return false
        if(y>width) return false
        return true
    }

    /**
     * Returns true if the given position is a bomb, false otherwise
     */
    private fun isBomb(x:Int,y:Int) =bombs.find {it==posToNumber(x,y)} !=null

    /**
     * Transforms XY coordonates to the XY number on the grid
     */
    private fun posToNumber(x:Int,y:Int) =(x-1)*width+y-1

    /**
     * Returns whether the current cell is either empty or checked
     */
    private fun isCleared(x:Int,y:Int) : Boolean
    {
        return positionsList[posToNumber(x,y)]==Possibilities.Empty || positionsList[posToNumber(x,y)]==Possibilities.Checked
    }

    /**
     * Counts the number of bombs on the same line or column
     */
    private fun countBombs(x:Int,y:Int) : Int
    {
        var count=0
        for(x0 in 1..width)
        {
            if(isBomb(x0,y))
                count++
        }
        for(y0 in 1..width)
        {
            if(isBomb(x,y0))
                count++
        }
        return count
    }

    /**
     * Clears the terrain recursively, starting from the current position and moving to its neighbors
     */
    private fun clear(number: Int)
    {
        val position=numberToPos(number)
        val x=position.first
        val y=position.second

        //count bombs
        //var bombCount=0
        //for(z in mooreNeighborhood)
        //{
        //    val curPos=Pair(x+z.first,y+z.second)
        //    if(isValid(curPos.first,curPos.second))
        //       if(isBomb(curPos.first,curPos.second))
        //           bombCount++
        //}

        var bombCount=countBombs(x,y)
        if(bombCount==0)
            textList[number]=""
        else textList[number]=bombCount.toString()
        invalidatedItems.add(number)

        //clear terrain
        positionsList[number]=Possibilities.Empty
        if(bombCount>0) return
        for(z in mooreNeighborhood)
        {
            val curPos=Pair(x+z.first,y+z.second)
            val curNumber=posToNumber(curPos.first,curPos.second)
            if(isValid(curPos.first,curPos.second))
              if(!isCleared(curPos.first,curPos.second))
                  if(!isBomb(curPos.first,curPos.second))
                    clear(curNumber)
        }
    }

    /**
     * Checks if the game has finished
     */
    private fun checkGameFinished()
    {
        if(!initGuessed)
            return
        var j=0
        var checkedJ=0
        for(i in 0 until size)
        {
            if(positionsList[i]==Possibilities.Unclicked || positionsList[i]==Possibilities.Checked)
            {
                j++
            }
            if(positionsList[i]==Possibilities.Checked)
                checkedJ++
        }

        if(checkedJ==bombs.size)
        {
            var correctlyChecked=true
            for(i in 0 until size)
            {
                if(positionsList[i]==Possibilities.Checked && bombs.find({it==i})==null)
                {
                    correctlyChecked=false
                }
            }
            if(correctlyChecked)
            {
                for(i in 0 until size)
                {
                    if(positionsList[i]==Possibilities.Unclicked)
                    {
                        clear(i)
                    }
                }
                gameEnded=true
            }
        }
        if(j==bombs.size)
        {
            for(i in 0 until size)
            {
                if(positionsList[i]==Possibilities.Unclicked)
                {
                    positionsList[i]=Possibilities.Checked
                    textList[i]="\uD83D\uDEA9"
                    invalidatedItems.add(i)
                }
            }
            gameEnded=true
        }
    }

    /**
     * Simulates a click on an unclicked cell
     */
    private fun click(number: Int)
    {
        if(!initGuessed)
        {
            initBombs(numBombs,number)
        }
        if (bombs.find {it==(number)} ==null)
        {
            clear(number)
            val pos=numberToPos(number)

            if(!initGuessed)
                for (z in mooreNeighborhood)
                {
                    val x=pos.first+z.first
                    val y=pos.second+z.second
                    if(isValid(x,y) && !isCleared(x,y) && !isBomb(x,y))
                        clear(posToNumber(x,y))
                }

            initGuessed=true
            checkGameFinished()
        }
        else
        {
            positionsList[number]=Possibilities.Bombed
            textList[number]="\uD83D\uDCA3"
            invalidatedItems.add(number)
            gameEnded=true
        }
    }

    /**
     * Checks the current cell
     */
    private fun check(number: Int)
    {
        positionsList[number]=Possibilities.Checked
        textList[number]="\uD83D\uDEA9"
        invalidatedItems.add(number)
        checkGameFinished()
    }

    /**
     * Unchecks the current cell
     */
    private fun uncheck(number: Int)
    {
        positionsList[number]=Possibilities.Unclicked
        textList[number]=""
        invalidatedItems.add(number)
        checkGameFinished()
    }

    /**
     * Updates the grid by taking a Click or Long Click action and responding accordingly
     */
    fun update(number: Int, longClick: Boolean)
    {
        if(gameEnded)
            return
        when(longClick){
            false -> when(positionsList[number])
            {
                Possibilities.Unclicked -> click(number)
                Possibilities.Empty, Possibilities.Bombed, Possibilities.Checked -> {}
            }
            true -> when(positionsList[number])
            {
                Possibilities.Unclicked -> check(number)
                Possibilities.Checked -> uncheck(number)
                Possibilities.Empty,Possibilities.Bombed -> {}
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(width)
        parcel.writeInt(numBombs)
        parcel.writeByte(if (gameEnded) 1 else 0)
        parcel.writeInt(size)
        parcel.writeStringArray(positionsList.map{ it.toString()}.toTypedArray())
        parcel.writeStringArray(textList.toTypedArray())
        parcel.writeIntArray(bombs.toIntArray())
        parcel.writeByte(if (initGuessed) 1 else 0)
        parcel.writeIntArray(invalidatedItems.toIntArray())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Logic> {
        override fun createFromParcel(parcel: Parcel): Logic {
            return Logic(parcel)
        }

        override fun newArray(size: Int): Array<Logic?> {
            return arrayOfNulls(size)
        }
    }
}