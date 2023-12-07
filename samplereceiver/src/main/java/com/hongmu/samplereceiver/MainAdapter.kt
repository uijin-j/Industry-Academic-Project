package com.hongmu.samplereceiver

import android.media.AudioManager
import android.media.ToneGenerator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hongmu.samplereceiver.databinding.ItemAlertBinding
import com.hongmu.samplereceiver.db.MyLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainAdapter: RecyclerView.Adapter<MainAdapter.ViewHolder>() {
    private val format = SimpleDateFormat("yyyy년MM월dd일 HH시mm분ss초", Locale.getDefault())

    var list: List<MyLog> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ViewHolder(private val binding: ItemAlertBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MyLog) {
            val context = binding.root.context

             when(item.type) {
                2 -> {
                    binding.textTitle.text = "위험 영역"
                    binding.color.setBackgroundColor(ContextCompat.getColor(context, R.color.yellow))
                }
                3 -> {
                    binding.textTitle.text = "위험 영역 내 빠른 이동 감지"
                    binding.color.setBackgroundColor(ContextCompat.getColor(context, R.color.red))

                }
                4 -> {
                    binding.textTitle.text = "낙상 영역"
                    binding.color.setBackgroundColor(ContextCompat.getColor(context, R.color.black))

                }
                else -> {
                    binding.textTitle.text = "안전 영역"
                    binding.color.setBackgroundColor(ContextCompat.getColor(context, R.color.white))

                }
            }
            binding.textDate.text = format.format(Date(item.date))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}