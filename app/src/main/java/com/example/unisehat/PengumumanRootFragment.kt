package com.example.unisehat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.unisehat.databinding.FragmentPengumumanRootBinding

class PengumumanRootFragment : Fragment() {

    private var _binding: FragmentPengumumanRootBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPengumumanRootBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val childFragment = PengumumanFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.pengumumanFragment_container, childFragment)
            .commit()
    }


}