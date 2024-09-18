package com.example.unisehat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.unisehat.databinding.FragmentPengumumanBinding
import com.example.unisehat.databinding.FragmentPeraturanBinding

class PengumumanFragment : Fragment() {

    private var _binding: FragmentPengumumanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPengumumanBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.peraturanBtn.setOnClickListener {
            navigateToFragment(PeraturanFragment())
        }
    }

    private fun navigateToFragment(fragment: Fragment){
        parentFragmentManager.beginTransaction()
            .replace(R.id.pengumumanFragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
