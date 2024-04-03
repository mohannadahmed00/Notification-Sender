package com.giraffe.mynotificationsender.features.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.giraffe.mynotificationsender.databinding.FragmentLoginBinding
import com.giraffe.mynotificationsender.FirebaseUtils
import com.giraffe.mynotificationsender.features.client.ClientModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth


class LoginFragment : Fragment() {
    companion object{
        private const val TAG = "LoginFragment"
    }
    private lateinit var binding: FragmentLoginBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser!=null){
            if (FirebaseAuth.getInstance().currentUser?.email=="digitalizer@notifysystem.com"){
                val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                findNavController().navigate(action)
            }else{
                val action = LoginFragmentDirections.actionLoginFragmentToClientFragment()
                findNavController().navigate(action)
            }

        }
        handleClicks()
    }

    private fun handleClicks() {
        binding.buttonLogin.setOnClickListener {
            if (isValidData()) {
                signIn(
                    binding.editTextEmail.text.trim().toString(),
                    binding.editTextPassword.text.trim().toString()
                )
            }
        }
        binding.tvDontHaveAccount.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            findNavController().navigate(action)
        }
    }

    private fun signIn(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    Toast.makeText(requireContext(),"email or password is invalid", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "signIn: ", it.exception)
                } else {
                    val userEmail= FirebaseAuth.getInstance().currentUser?.email
                    if(userEmail == "digitalizer@notifysystem.com" || userEmail == "ikrimah.sh@gmail.com"){
                        val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                        findNavController().navigate(action)
                    }else if(FirebaseAuth.getInstance().currentUser?.isEmailVerified == true){
                        saveUserData(FirebaseAuth.getInstance().currentUser?.email?:"")
                    }else{
                        FirebaseAuth.getInstance().signOut()
                        Log.e(TAG, "signIn: email or password is invalid (client)")
                        Toast.makeText(requireContext(),"email or password is invalid", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun saveUserData(email: String) {
        FirebaseUtils.currentUserDetails().set(ClientModel(FirebaseUtils.currentUserId()?:"",email, Timestamp.now()))
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    Toast.makeText(requireContext(),"something went wrong",Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "saveUserData: ", it.exception)
                } else {
                    val action = LoginFragmentDirections.actionLoginFragmentToClientFragment()
                    findNavController().navigate(action)
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