package com.example.mobil

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.navigation.fragment.navArgs
import com.example.mobil.model.Card
import com.google.firebase.firestore.*
import java.util.*
import kotlin.collections.ArrayList

class CardFragment : Fragment() {
    private val argsCard: CardFragmentArgs by navArgs()
    private var cards = ArrayList<Card>()
    private lateinit var database: FirebaseFirestore

    lateinit var frontAnimator: AnimatorSet
    lateinit var backAnimator: AnimatorSet
    lateinit var fastFrontAnimator: AnimatorSet
    lateinit var fastBackAnimator: AnimatorSet
    var isFront = true


    lateinit var tts: TextToSpeech

    private var index = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_card, container, false)
        index = 0


        // Text-To_speech
        tts = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.US
            }
        })

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Loads non-ignored cards into a local navigable ArrayList
        loadDeck()

        // Card Front and Back
        var cardFront = view.findViewById<CardView>(R.id.card_view_question)
        var cardBack = view.findViewById<CardView>(R.id.card_view)

        // Animators
        frontAnimator = AnimatorInflater.loadAnimator(context, R.animator.front_animation) as AnimatorSet
        backAnimator = AnimatorInflater.loadAnimator(context, R.animator.back_animation) as AnimatorSet
        fastFrontAnimator = AnimatorInflater.loadAnimator(context, R.animator.fast_front_animation) as AnimatorSet
        fastBackAnimator = AnimatorInflater.loadAnimator(context, R.animator.fast_back_animation) as AnimatorSet

        // Text-To-Speech Button
        val ttsButtonFront = view.findViewById<Button>(R.id.ttsButtonQuestion)
        val ttsButtonBack = view.findViewById<Button>(R.id.ttsButton)
        ttsButtonFront.setOnClickListener { ttsFunction(view) }
        ttsButtonBack.setOnClickListener { ttsFunction(view) }

        // Hamburger Menu Button
        val menuFront : ImageView = view.findViewById(R.id.cardHamburgerMenuQuestion)
        val menuBack : ImageView = view.findViewById(R.id.cardHamburgerMenu)
        menuFront.setOnClickListener{ popupMenu(menuFront)}
        menuBack.setOnClickListener{ popupMenu(menuBack) }

        // Previous Card button
        val previousCardBtn = view.findViewById<Button>(R.id.previousCardButton)
        previousCardBtn.setOnClickListener {
            if (index == 0) {
                index = cards.size - 1
            } else {
                index -= 1
            }
            if (isFront) {
                showCard()
            } else {
                flipToQuestionFast(cardFront,cardBack)
            }
            stopTTS()
            isFront = true
        }

        // Next Card Button
        val nextCardBtn = view.findViewById<Button>(R.id.nextCardButton)
        nextCardBtn.setOnClickListener {
            if (index == cards.size - 1) {
                index = 0
            } else {
                index += 1
            }
            if (isFront) {
                showCard()
            } else {
                flipToQuestionFast(cardFront,cardBack)
            }
            stopTTS()
            isFront = true
        }


        // Flip Card Button
        val flipCardBtn = view.findViewById<Button>(R.id.flipCardButton)
        flipCardBtn.setOnClickListener {
            if (isFront) {
                showCard()
                flipToAnswer(cardFront,cardBack)

            } else {
                showCard()
                flipToQuestion(cardFront,cardBack)
            }
            stopTTS()

        }

    }

    private fun ttsFunction(view: View){
        var toSpeak = ""
        if (isFront) {
            toSpeak = view.findViewById<TextView>(R.id.cardTextView).text.toString()
        }
        else {
            toSpeak = view.findViewById<TextView>(R.id.cardTextViewAnswer).text.toString()
        }

        if (toSpeak == "") {
            // Toast.makeText(this,"Enter text", Toast.LENGTH_SHORT).show()
            Toast.makeText(context,"Enter text", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(context, toSpeak, Toast.LENGTH_SHORT).show()
            tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "1")
        }
    }

    // Hamburger Menu
    private fun popupMenu(menuView : View) {

        val popupMenu = PopupMenu(menuView.context, menuView)
        popupMenu.inflate(R.menu.card_hamburger_menu)
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){

                // Ignore Button Functionality
                R.id.cardIgnore->{
                    val cardID = cards[index].docId
                    var ignored = cards[index].isIgnored

                    ignored = !ignored!!

                    val card = hashMapOf(
                        "question" to cards[index].question,
                        "answer" to cards[index].answer,
                        "isIgnored" to ignored
                    )
                    database.collection("Decks")
                        .document(argsCard.deckId.toString())
                        .collection("cards")
                        .document(cardID.toString())
                        .set(card)

                    if (ignored) {
                        cards.removeAt(index)
                        if (index == 0) {
                            index = cards.size - 1
                        } else {
                            index--
                        }
                    }
                    else {
                        cards[index].isIgnored = ignored
                    }
                    showCard()
                    stopTTS()
                    true
                }

                // Edit Button Functionality
                R.id.cardEdit->{

                    val inflater = LayoutInflater.from(context).inflate(R.layout.add_card, null)
                    val addQuestion = inflater.findViewById<EditText>(R.id.enter_question)
                    val addAnswer = inflater.findViewById<EditText>(R.id.enter_answer)

                    addQuestion.setText(cards[index].question)
                    addAnswer.setText(cards[index].answer)

                    val addCardDialog = android.app.AlertDialog.Builder(context)
                    addCardDialog.setView(inflater)

                    addCardDialog.setPositiveButton("Save") {
                            dialog,_->
                        val cardID = cards[index].docId
                        val question = addQuestion.text.toString()
                        val answer = addAnswer.text.toString()
                        val card = hashMapOf(
                            "question" to question,
                            "answer" to answer,
                            "isIgnored" to cards[index].isIgnored,
                        )
                        database.collection("Decks")
                            .document(argsCard.deckId.toString())
                            .collection("cards")
                            .document(cardID.toString())
                            .set(card)

                        cards[index] = Card(question, answer, cards[index].isIgnored, cardID)
                        showCard()
                        dialog.dismiss()
                    }

                    addCardDialog.setNegativeButton("Cancel") {
                            dialog,_->
                        dialog.dismiss()
                    }
                    addCardDialog.create()
                    addCardDialog.show()
                    stopTTS()

                    true
                }

                // Delete Button Functionality
                R.id.cardDelete->{
                    val inflater = LayoutInflater.from(context).inflate(R.layout.delete_card, null)

                    val deleteDialog = android.app.AlertDialog.Builder(context)
                    deleteDialog.setView(inflater)

                    deleteDialog.setPositiveButton("Delete") {
                            dialog,_->

                        database.collection("Decks")
                            .document(argsCard.deckId.toString())
                            .collection("cards")
                            .document(cards[index].docId.toString())
                            .delete()
                        cards.removeAt(index)
                        if (index == 0) {
                            index = cards.size - 1
                        } else {
                            index -= 1
                        }
                        showCard()
                        dialog.dismiss()
                        stopTTS()
                    }

                    deleteDialog.setNegativeButton("Cancel"){
                            dialog,_->
                        dialog.dismiss()
                    }
                    deleteDialog.create()
                    deleteDialog.show()
                    true
                }
                else -> true
            }
        }
        popupMenu.show()
    }

    //Loads deck from firestore
    private fun loadDeck() {
        database = FirebaseFirestore.getInstance()
        cards = ArrayList<Card>()
        var cardIndex = 0
        var cardQuestion = ""

        database.collection("Decks").document(argsCard.deckId.toString()).collection("cards").get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    if (document.id == argsCard.cardId) {
                        index = cardIndex
                        cardQuestion = document.data.getValue("question").toString()
                    }
                    if (document.data.getValue("isIgnored") == false || document.id == argsCard.cardId) {
                        cards.add(
                            Card(
                                document.data.getValue("question") as String?,
                                document.data.getValue("answer") as String?,
                                document.data.getValue("isIgnored") as Boolean?,
                                document.id
                            )
                        )
                        cardIndex++
                    }
                }
                if (argsCard.shuffle) {
                    cards.shuffle()
                    cardIndex = 0
                    for (card in cards) {
                        if (card.question == cardQuestion) {
                            index = cardIndex
                        }
                        cardIndex++
                    }
                }
                showCard()
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }

    private fun flipToQuestion(cardFront: CardView, cardBack:CardView) {
        frontAnimator.setTarget(cardBack)
        backAnimator.setTarget(cardFront)
        frontAnimator.start()
        backAnimator.start()
        cardFront.bringToFront()
        isFront = true
    }
    private fun flipToQuestionFast(cardFront: CardView, cardBack:CardView) {
        fastFrontAnimator.setTarget(cardBack)
        fastBackAnimator.setTarget(cardFront)
        fastFrontAnimator.start()
        fastBackAnimator.start()
        cardFront.bringToFront()
        isFront = true
        // Necesarry to not spoil the answer of the next card
        fastFrontAnimator.doOnEnd { showCard() }
    }
    private fun flipToAnswer(cardFront: CardView, cardBack:CardView) {
        frontAnimator.setTarget(cardFront)
        backAnimator.setTarget(cardBack)
        frontAnimator.start()
        backAnimator.start()
        cardBack.bringToFront()
        isFront = false
    }

    // Shows the correct card and symbol
    private fun showCard() {
        view?.findViewById<TextView>(R.id.cardTextView)?.text = cards[index].question
        view?.findViewById<TextView>(R.id.cardTextViewAnswer)?.text = cards[index].answer


        // Checks if card is ignored, to determine if we are displaying the "ignored" icon
        val cardImageQuestion = view?.findViewById<ImageView>(R.id.cardIgnoreImageViewQuestion)
        val cardImageAnswer = view?.findViewById<ImageView>(R.id.cardIgnoreImageView)
        if(cards[index].isIgnored == true) {
            cardImageQuestion?.setImageResource(R.drawable.ic_baseline_ignore_24)
            cardImageAnswer?.setImageResource(R.drawable.ic_baseline_ignore_24)
        }
        else {
            cardImageQuestion?.setImageResource(R.drawable.ic_question_mark)
            cardImageAnswer?.setImageResource(R.drawable.ic_exclamation_mark)
        }
    }

    private fun stopTTS() {
        if (tts != null) {
            tts.stop()
        }
    }

    override fun onPause() {
        stopTTS()
        super.onPause()
    }

    override fun onDestroy() {
        if (tts != null) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}

