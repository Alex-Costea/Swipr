package com.example.swipr

import kotlin.random.Random

class Logic (width : Int,numBombs:Int) {
    var positionsList: MutableList<Possibilities>
    var textList:MutableList<String>
    private var size : Int
    private var GameEnded:Boolean
    private var InitGuessed:Boolean
    private var Bombs:MutableList<Int>
    private var width:Int = width
    private var numBombs:Int
    private val mooreNeighborhood =
        mutableListOf(Pair(-1,-1),Pair(-1,0),Pair(-1,1),Pair(0,-1),Pair(0,1),Pair(1,-1),Pair(1,0),Pair(1,1))
    private var invalidatedItems:MutableList<Int>
    var updateItems:MutableList<Int>
    get() {val y=invalidatedItems;invalidatedItems = mutableListOf(); return y;}
    set(value){invalidatedItems=value}
    init {
        GameEnded=false
        size=width*width
        positionsList= mutableListOf()
        textList= mutableListOf()
        this.numBombs=numBombs
        for(i in 1..size)
        {
            positionsList.add(Possibilities.Unclicked)
            textList.add("")
        }
        Bombs=mutableListOf()
        InitGuessed=false
        invalidatedItems= mutableListOf()
    }

    /**
     * Resets the game
     */
    fun reset()
    {
        GameEnded=false
        positionsList= mutableListOf()
        textList= mutableListOf()
        for(i in 1..size)
        {
            positionsList.add(Possibilities.Unclicked)
            textList.add("")
        }
        Bombs=mutableListOf()
        InitGuessed=false
        invalidatedItems= mutableListOf()
    }

    /**
     * Initiates the location of the bombs
     */
    private fun initBombs(nrBombs:Int,initGuess:Int)
    {
        Bombs=mutableListOf(initGuess)
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
                Bombs.add(currentBomb)
                bombsCleared++
            }
        }
        for(i in 0 until nrBombs)
        {
            val bombLocation=Random.nextInt(0,size-i-bombsCleared)
            var j=0
            for(j2 in 0 until Bombs.size)
            {
                if(Bombs[j2]>bombLocation+j)
                {
                    break
                }
                j++
            }
            Bombs.add(j,bombLocation+j)
        }
        Bombs.remove(initGuess)
        for(x in mooreNeighborhood)
        {
            val i=x.first
            val j=x.second
            if(isValid(initGuessX+i,initGuessY+j))
            {
                val currentBomb = posToNumber(initGuessX + i, initGuessY + j)
                Bombs.remove(currentBomb)
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
    private fun isBomb(x:Int,y:Int) =Bombs.find {it==posToNumber(x,y)} !=null

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
        if(!InitGuessed)
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

        if(checkedJ==Bombs.size)
        {
            var correctlyChecked=true
            for(i in 0 until size)
            {
                if(positionsList[i]==Possibilities.Checked && Bombs.find({it==i})==null)
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
                GameEnded=true
            }
        }
        if(j==Bombs.size)
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
            GameEnded=true
        }
    }

    /**
     * Simulates a click on an unclicked cell
     */
    private fun click(number: Int)
    {
        if(!InitGuessed)
        {
            initBombs(numBombs,number)
        }
        if (Bombs.find({it==(number)})==null)
        {
            clear(number)
            val pos=numberToPos(number)

            if(!InitGuessed)
                for (z in mooreNeighborhood)
                {
                    val x=pos.first+z.first
                    val y=pos.second+z.second
                    if(isValid(x,y) && !isCleared(x,y) && !isBomb(x,y))
                        clear(posToNumber(x,y))
                }

            InitGuessed=true
            checkGameFinished()
        }
        else
        {
            positionsList[number]=Possibilities.Bombed
            textList[number]="\uD83D\uDCA3"
            invalidatedItems.add(number)
            GameEnded=true
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
        if(GameEnded)
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
}