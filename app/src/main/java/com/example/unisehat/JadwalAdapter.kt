package com.example.unisehat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.unisehat.models.Jadwal
class JadwalAdapter(private val jadwalList: List<Jadwal>) : RecyclerView.Adapter<JadwalAdapter.ViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_jadwal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(jadwalList[position], position == selectedPosition)
        holder.itemView.setOnClickListener {
            notifyItemChanged(selectedPosition)
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount(): Int {
        return jadwalList.size
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tanggalTextView: TextView = itemView.findViewById(R.id.tanggalTextView)
        private val jamTextView: TextView = itemView.findViewById(R.id.jamTextView)
        private val hariTextView: TextView = itemView.findViewById(R.id.hariTextView)

        fun bind(jadwal: Jadwal, isSelected: Boolean) {
            itemView.isSelected = isSelected
            tanggalTextView.text = jadwal.tanggal
            jamTextView.text = jadwal.jam
            hariTextView.text = jadwal.hari
        }
    }
}
