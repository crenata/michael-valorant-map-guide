package com.scranaver.valorantmapguide.ui.dashboard

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.scranaver.valorantmapguide.R
import com.scranaver.valorantmapguide.data.Constants
import com.scranaver.valorantmapguide.databinding.FragmentDashboardBinding
import com.scranaver.valorantmapguide.ui.dashboard.rank.Rank
import com.scranaver.valorantmapguide.ui.dashboard.rank.RankAdapter
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("NotifyDataSetChanged")
class DashboardFragment : Fragment() {
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var auth: FirebaseAuth = Firebase.auth

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var loading: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var personalInfoButton: TextView
    private lateinit var rankHistoryButton: TextView
    private lateinit var personalInfoLinearLayout: LinearLayout
    private lateinit var rankHistoryLinearLayout: LinearLayout

    private lateinit var imageProfile: ShapeableImageView
    private lateinit var nameProfile: TextView
    private lateinit var emailProfile: TextView

    private lateinit var editNameButton: ImageView
    private lateinit var nameLinearLayout: LinearLayout
    private lateinit var newNameLinearLayout: LinearLayout
    private lateinit var newNameEditText: EditText
    private lateinit var saveNameButton: Button

    private lateinit var currentRank: TextView
    private lateinit var addRankInformationButton: TextView
    private lateinit var rankInformationLinearLayout: LinearLayout
    private lateinit var newRankInformationEditText: EditText
    private lateinit var saveRankInformationButton: Button

    private var rankList = ArrayList<Rank>()
    private lateinit var rankAdapter: RankAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        binding.rankRecyclerview.layoutManager = LinearLayoutManager(context)
        rankAdapter = RankAdapter(rankList)
        binding.rankRecyclerview.adapter = rankAdapter

        loading = binding.loading
        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            getProfile()
            getRankHistory()
        }

        imageProfile = binding.profile.imageProfile
        nameProfile = binding.profile.name
        emailProfile = binding.email.email

        imageProfile.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, Constants.selectImage())
        }

        editNameButton = binding.profile.editName
        nameLinearLayout = binding.profile.linearName
        newNameLinearLayout = binding.profile.linearNewName
        newNameEditText = binding.profile.newName
        saveNameButton = binding.profile.saveNewName
        editNameButton.setOnClickListener {
            newNameLinearLayout.visibility = View.VISIBLE
            nameLinearLayout.visibility = View.GONE
        }
        saveNameButton.setOnClickListener {
            changeName()
        }

        personalInfoButton = binding.profile.personalInfo
        rankHistoryButton = binding.profile.rankHistory
        personalInfoLinearLayout = binding.linearPersonalInfo
        rankHistoryLinearLayout = binding.linearRankHistory
        personalInfoButton.setOnClickListener {
            personalInfoLinearLayout.visibility = View.VISIBLE
            rankHistoryLinearLayout.visibility = View.GONE
        }
        rankHistoryButton.setOnClickListener {
            personalInfoLinearLayout.visibility = View.GONE
            rankHistoryLinearLayout.visibility = View.VISIBLE
        }

        currentRank = binding.rank.currentRank
        addRankInformationButton = binding.rank.addRankInformation
        rankInformationLinearLayout = binding.rank.linearNewRankInformation
        newRankInformationEditText = binding.rank.newRankInformation
        saveRankInformationButton = binding.rank.saveNewRankInformation
        addRankInformationButton.setOnClickListener {
            rankInformationLinearLayout.visibility = View.VISIBLE
        }
        saveRankInformationButton.setOnClickListener {
            addNewRank()
        }

        getProfile()
        getRankHistory()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.selectImage() && resultCode == RESULT_OK && data != null && data.data != null) {
            val progressDialog = ProgressDialog(context)
            progressDialog.setCancelable(false)
            progressDialog.show()
            val now = System.currentTimeMillis()
            val contentResolver = context?.contentResolver
            val mimeType = MimeTypeMap.getSingleton()
            val extension = mimeType.getExtensionFromMimeType(contentResolver?.getType(data.data!!))
            val storageReference = FirebaseStorage.getInstance().reference.child("users/$now.$extension")
            storageReference.putFile(data.data!!).addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val user = auth.currentUser
                    if (user != null) {
                        db.collection(Constants.userCollectionKey()).document(user.uid).update(
                            hashMapOf(
                                "image" to uri.toString()
                            ) as Map<String, Any>
                        ).addOnSuccessListener {
                            progressDialog.dismiss()
                            getProfile()
                        }.addOnFailureListener { error ->
                            progressDialog.dismiss()
                            Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { error ->
                    progressDialog.dismiss()
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { error ->
                progressDialog.dismiss()
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }.addOnProgressListener { taskSnapshot ->
                val percent = (100.00 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                progressDialog.setMessage("Uploading $percent")
            }
        }
    }

    private fun getProfile() {
        db.document("${Constants.userCollectionKey()}/${auth.currentUser?.uid}").get().addOnSuccessListener { snapshot ->
            val data = snapshot.data
            nameProfile.text = data?.get("name").toString()
            emailProfile.text = data?.get("email").toString()
            Glide.with(this).load(data?.get("image").toString()).placeholder(R.drawable.ic_baseline_person_24).into(imageProfile)
        }.addOnFailureListener { error ->
            Log.e(Constants.logKey(), "Error: ", error)
        }
        swipeRefreshLayout.isRefreshing = false
    }

    private fun getRankHistory() {
        rankList.clear()
        db.collection("${Constants.userCollectionKey()}/${auth.currentUser?.uid}/${Constants.rankCollectionKey()}").orderBy("created_at",  Query.Direction.DESCENDING).get().addOnSuccessListener { collectionRanks ->
            for (rank in collectionRanks.documents) {
                val data = rank.data
                rankList.add(Rank(data?.get("rank").toString()))
            }
            collectionRanks.documents.indexOfFirst { snapshot ->
                if (snapshot != null) {
                    val lastData = snapshot.data
                    currentRank.text = lastData?.get("rank").toString()
                }
                true
            }
            rankAdapter.notifyDataSetChanged()
        }.addOnFailureListener { error ->
            Log.e(Constants.logKey(), "Error: ", error)
        }
        swipeRefreshLayout.isRefreshing = false
    }

    private fun changeName() {
        val newName: String = newNameEditText.text.toString()

        if (newName.isBlank()) {
            newNameEditText.error = "Name is required!"
            newNameEditText.requestFocus()
            return
        }

        loading.visibility = View.VISIBLE
        saveNameButton.isEnabled = false

        val user = auth.currentUser
        if (user != null) {
            db.collection(Constants.userCollectionKey()).document(user.uid).update(
                hashMapOf(
                    "name" to newName
                ) as Map<String, Any>
            ).addOnSuccessListener {
                loading.visibility = View.GONE
                saveNameButton.isEnabled = true
                newNameLinearLayout.visibility = View.GONE
                nameLinearLayout.visibility = View.VISIBLE
                Toast.makeText(context, R.string.success, Toast.LENGTH_LONG).show()
                getProfile()
            }.addOnFailureListener { error ->
                loading.visibility = View.GONE
                saveNameButton.isEnabled = true
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            loading.visibility = View.GONE
            saveNameButton.isEnabled = true
            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addNewRank() {
        val newRankInformation: String = newRankInformationEditText.text.toString()

        if (newRankInformation.isBlank()) {
            newRankInformationEditText.error = "Rank is required!"
            newRankInformationEditText.requestFocus()
            return
        }

        loading.visibility = View.VISIBLE
        addRankInformationButton.isEnabled = false

        val user = auth.currentUser
        if (user != null) {
            db.collection(Constants.userCollectionKey()).document(user.uid).collection(Constants.rankCollectionKey()).add(hashMapOf(
                "rank" to newRankInformation,
                "created_at" to Timestamp(Date())
            )).addOnSuccessListener {
                loading.visibility = View.GONE
                addRankInformationButton.isEnabled = true
                rankInformationLinearLayout.visibility = View.GONE
                Toast.makeText(context, R.string.success, Toast.LENGTH_LONG).show()
                getRankHistory()
            }.addOnFailureListener { error ->
                loading.visibility = View.GONE
                addRankInformationButton.isEnabled = true
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            loading.visibility = View.GONE
            addRankInformationButton.isEnabled = true
            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }
}