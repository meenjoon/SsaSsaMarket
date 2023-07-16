package com.mbj.ssassamarket.ui.chat.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentChatDetailBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.util.EventObserver
import com.mbj.ssassamarket.util.LocateFormat
import com.mbj.ssassamarket.util.LocationManager
import com.mbj.ssassamarket.util.location.LocationFormat.parseLatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPoint

@AndroidEntryPoint
class ChatDetailFragment : BaseFragment(), LocationManager.LocationUpdateListener {
    override val binding get() = _binding as FragmentChatDetailBinding
    override val layoutId: Int get() = R.layout.fragment_chat_detail

    private lateinit var adapter: ChatDetailAdapter
    private val args: ChatDetailFragmentArgs by navArgs()
    private val viewModel: ChatDetailViewModel by viewModels()
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = LocationManager(requireContext(), 10000L, 10000.0F, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        adapter = ChatDetailAdapter()
        binding.chatDetailRv.adapter = adapter
        args.otherImageColor?.let { adapter.updateOtherImageColor(it) }
        setupViewModel()
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        locationManager.startLocationTracking()
    }

    private fun setupViewModel() {
        setChatRoomId()
        addChatDetailEventListener()
        getMyDataId()
        getMyUserItem()
        getOtherUserItem()
        observeChatItemList()
        observeOtherUserItem()
        observeLatLngString()
        observeDistanceDiff()
        observeMyUserDataError()
        observeOtherUserDataError()
        observeSendMessageError()
    }

    private fun setChatRoomId() {
        val chatRoomId = args.chatRoomItem?.chatRoomId ?: args.chatRoomId.toString()
        viewModel.setChatRoomId(chatRoomId)
    }

    private fun addChatDetailEventListener() {
        lifecycleScope.launch {
            viewModel.addChatDetailEventListener()
        }
    }

    private fun getMyDataId() {
        viewModel.getMyDataId()
    }

    private fun getMyUserItem() {
        viewModel.getMyUserItem()
    }

    private fun getOtherUserItem() {
        val otherUserId = args.chatRoomItem?.otherUserId ?: args.otherId.toString()
        viewModel.setOtherUserId(otherUserId)
        viewModel.getOtherUserItem(otherUserId)
    }

    private fun observeChatItemList() {
        viewModel.chatItemList.observe(viewLifecycleOwner, EventObserver { chatItemList ->
            adapter.submitList(chatItemList)

            if (chatItemList.isNotEmpty()) {
                binding.chatDetailRv.post {
                    binding.chatDetailRv.smoothScrollToPosition(chatItemList.size - 1)
                }
            }
        })
    }

    private fun setupUI() {
        binding.chatDetailBackIv.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.chatDetailSendIv.setOnClickListener {
            val message = binding.chatDetailTiev.text.toString()
            if (message.isEmpty()) {
                showToast(R.string.empty_message_error)
                return@setOnClickListener
            }
            viewModel.sendMessage(message, viewModel.myDataId.value)
            binding.chatDetailTiev.text?.clear()
        }
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }

    override fun onLocationUpdated(latitude: Double?, longitude: Double?) {
        if (latitude != null && longitude != null) {
            val latLngString = "$latitude $longitude"
            viewModel.setLatLng(latLngString)
            val mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
            val reverseGeoCoder = locationManager.createReverseGeoCoder(requireActivity(),mapPoint) { addressString ->
                val location = LocateFormat.getSelectedAddress(addressString, 2)
                viewModel.setLocation(location)
            }
            reverseGeoCoder.startFindingAddress()
        }
    }

    private fun observeOtherUserItem() {
        viewModel.otherUserItem.observe(viewLifecycleOwner, EventObserver { otherUserItem ->
            adapter.updateOtherUserItem(otherUserItem)
            binding.chatDetailNicknameAbTv.text = otherUserItem.userName

            viewModel.setOtherUserItemState(otherUserItem)
            viewModel.processLocationData()

            val coordinates = parseLatLng(otherUserItem.latLng)
            if(coordinates != null) {
                val (latitude, longitude) = coordinates
                val mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                val reverseGeoCoder = locationManager.createReverseGeoCoder(requireActivity(),mapPoint) { addressString ->
                    val location = LocateFormat.getSelectedAddress(addressString, 2)
                    binding.chatDetailLocationTv.text = location
                }
                reverseGeoCoder.startFindingAddress()
            }
        })
    }

    private fun observeLatLngString() {
        viewModel.latLngString.observe(viewLifecycleOwner, EventObserver { myLatLng ->
            viewModel.setLngState(myLatLng)
            viewModel.processLocationData()
        })
    }

    private fun observeDistanceDiff() {
        viewModel.distanceDiff.observe(viewLifecycleOwner, EventObserver { distanceDiff ->
            binding.chatDetailLocationDiffTv.text = distanceDiff
        })
    }

    private fun observeMyUserDataError() {
        viewModel.myUserDataError.observe(viewLifecycleOwner, EventObserver { myUserDataError ->
            if (myUserDataError) {
                showToast(R.string.error_message_user_data)
            }
        })
    }

    private fun observeOtherUserDataError() {
        viewModel.otherUserDataError.observe(viewLifecycleOwner, EventObserver { otherUserDataError ->
            if (otherUserDataError) {
                showToast(R.string.error_message_user_data)
            }
        })
    }

    private fun observeSendMessageError() {
        viewModel.sendMessageError.observe(viewLifecycleOwner, EventObserver { sendMessageError ->
            if (sendMessageError) {
                showToast(R.string.error_message_send_message)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        locationManager.stopLocationTracking()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeChatDetailEventListener()
    }
}

