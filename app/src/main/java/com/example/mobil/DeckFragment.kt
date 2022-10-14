package com.example.mobil

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobil.adapter.CardsAdapter
import com.example.mobil.model.Card
import kotlin.random.Random

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DeckFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DeckFragment : Fragment() {
    private var cards = ArrayList<Card>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_deck, container, false)

        // adapter & recycler
        val cardsAdapter = CardsAdapter(context = DeckActivity(), cards)
        val cardsRecycler = view.findViewById<RecyclerView>(R.id.CardsRecyclerView)
        cardsRecycler.adapter = cardsAdapter
        cardsRecycler.layoutManager = LinearLayoutManager(context)

        //ToDo: Load deck instead of creating cards here
        cards.add(Card(0, "Card 0 Question", "Card 0 Answer", 0, false))
        cards.add(Card(1, "Card 1 Question", "Card 1 Answer", 0, false))
        cards.add(Card(2, "Card 2 Question", "Card 2 Answer", 0, false))
        cards.add(Card(3, "Card 3 Question", "Card 3 Answer", 0, false))
        cards.add(Card(4, "Card 4 Question", "Card 4 Answer", 0, false))


        // Add button
        val addBtn = view.findViewById<Button>(R.id.addCardButton)
        addBtn.setOnClickListener {
            val inflater = LayoutInflater.from(context).inflate(R.layout.add_card, null)
            val addQuestion = inflater.findViewById<EditText>(R.id.enter_question)
            val addAnswer = inflater.findViewById<EditText>(R.id.enter_answer)

            val addCardDialog = AlertDialog.Builder(context)
            addCardDialog.setView(inflater)

            addCardDialog.setPositiveButton("Save") {
                    dialog,_->
                val question = addQuestion.text.toString()
                val answer = addAnswer.text.toString()
                cards.add(Card(1, question, answer, 1, false))
                cardsAdapter.notifyDataSetChanged()
                dialog.dismiss()
            }

            addCardDialog.setNegativeButton("Cancel") {
                    dialog,_->
                dialog.dismiss()
            }
            addCardDialog.create()
            addCardDialog.show()
        }

        // Shuffle button
        val shuffleBtn = view.findViewById<Button>(R.id.shuffleButton)
        shuffleBtn.setOnClickListener {
            val shuffleIndex = Random.nextInt(cards.size)
            val shuffleElement = cards[shuffleIndex]

            //ToDo: This now needs to redirect to a fragment
            (activity as DeckActivity).replaceFragment(CardFragment())
            //val intent = Intent(this@DeckFragment.context, CardActivity::class.java)
            //intent.putExtra(shuffleElement.question, shuffleElement.answer)
            //startActivity(intent)
        }


        // Edit button
        val editBtn = view.findViewById<Button>(R.id.editModeButton)
        editBtn.setOnClickListener {
            (activity as DeckActivity).replaceFragment(EditFragment())
        }

        // Go to card
        cardsAdapter.setOnCardClickListener(object : CardsAdapter.onCardClickListener{
            override fun onCardClick(position: Int) {
                //ToDo: This now needs to work with fragment
                (activity as DeckActivity).replaceFragment(CardFragment())
                //startActivity(Intent(this@DeckFragment.context, CardActivity::class.java))
            }
        })
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DeckFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DeckFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}