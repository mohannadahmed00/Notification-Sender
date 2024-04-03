package com.giraffe.mynotificationsender.features.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import com.giraffe.mynotificationsender.R
import com.giraffe.mynotificationsender.databinding.FragmentSignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.messaging

class SignUpFragment : Fragment() {
    companion object {
        private const val TAG = "SignUpFragment"
    }
    private lateinit var binding:FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentSignUpBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        handleClicks()
    }


    private fun handleClicks() {
        binding.buttonSignUp.setOnClickListener {
            if (isValidData()) {
                createAccount(
                    binding.editTextEmail.text.trim().toString(),
                    binding.editTextPassword.text.trim().toString()
                )
            }else{
                Toast.makeText(requireContext(),"please complete your data", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tvHaveAccount.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun createAccount(email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    Toast.makeText(requireContext(),"something went wrong", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "createAccount: ", it.exception)
                } else {
                    FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener {task->
                            if (!task.isSuccessful) {
                                Toast.makeText(requireContext(),"something went wrong", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, "sendEmailVerification: ", it.exception)
                            }else{
                                Toast.makeText(requireContext(),"verification mail has been sent", Toast.LENGTH_SHORT).show()
                                Firebase.messaging.deleteToken()
                                FirebaseAuth.getInstance().signOut()
                                findNavController().navigateUp()
                            }
                        }
                }
            }
    }

    private fun isValidData(): Boolean {
        if (binding.editTextEmail.text.toString().isBlank()) {
            return false
        }
        if (binding.editTextPassword.text.toString().isBlank()) {
            return false
        }
        return true
    }

}