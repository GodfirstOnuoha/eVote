package com.chibufirst.evote.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.chibufirst.evote.MainActivity
import com.chibufirst.evote.R
import com.chibufirst.evote.adapters.CandidatesRecyclerAdapter
import com.chibufirst.evote.databinding.FragmentAdminCandidateBinding
import com.chibufirst.evote.models.Student
import com.chibufirst.evote.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.*

class AdminCandidateFragment : Fragment() {

    private var binding: FragmentAdminCandidateBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var firestoreListener: ListenerRegistration
    private val args: AdminCandidateFragmentArgs by navArgs()

    companion object {
        private val TAG: String = AdminCandidateFragment::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAdminCandidateBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        db = Firebase.firestore

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentSession = "${currentYear - 1}/$currentYear"

        binding!!.apply {
            titleText.text = getString(R.string.elections, currentSession)
            positionTextView.text = args.position
            positionTextView.setOnClickListener { requireActivity().onBackPressed() }
            logoutImage.setOnClickListener {
                auth.signOut()
                requireActivity().startActivity(Intent(requireActivity(), MainActivity::class.java))
                requireActivity().finish()
            }

            db.collection(Constants.USERS)
                .whereEqualTo(Constants.POSITION, args.position)
                .get()
                .addOnSuccessListener { documents ->
                    val candidateList = arrayListOf<Student>()
                    for (candidate in documents) {
                        val student = candidate.toObject<Student>()
                        candidateList.add(student)
                    }
                    if (candidateList.isEmpty()) {
                        candidatesRecycler.visibility = View.GONE
                        errorLayout.visibility = View.VISIBLE
                        errorText.visibility = View.VISIBLE
                        loadingProgress.visibility = View.GONE
                        errorText.text = getString(R.string.no_candidates, args.position)
                    } else {
                        candidatesRecycler.visibility = View.VISIBLE
                        errorLayout.visibility = View.GONE

                        val candidatesRecyclerAdapter =
                            CandidatesRecyclerAdapter(auth, db, candidateList)
                        candidatesRecycler.adapter = candidatesRecyclerAdapter

                    }
                }
                .addOnFailureListener { exception ->
                    candidatesRecycler.visibility = View.GONE
                    errorLayout.visibility = View.VISIBLE
                    errorText.text = exception.message
                }
        }

        firestoreListener = db.collection(Constants.USERS)
            .whereEqualTo(Constants.POSITION, args.position)
            .addSnapshotListener(EventListener { value, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed!", error)
                    return@EventListener
                }

                val candidateList = arrayListOf<Student>()
                for (candidate in value!!) {
                    val student = candidate.toObject<Student>()
                    candidateList.add(student)
                }
                binding!!.apply {
                    if (candidateList.isEmpty()) {
                        candidatesRecycler.visibility = View.GONE
                        errorLayout.visibility = View.VISIBLE
                        errorText.visibility = View.VISIBLE
                        loadingProgress.visibility = View.GONE
                        errorText.text = getString(R.string.no_candidates, args.position)
                    } else {
                        candidatesRecycler.visibility = View.VISIBLE
                        errorLayout.visibility = View.GONE

                        val candidatesRecyclerAdapter =
                            CandidatesRecyclerAdapter(auth, db, candidateList)
                        candidatesRecycler.adapter = candidatesRecyclerAdapter
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        firestoreListener.remove()
        binding = null
    }

}