package com.swm.smartattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swm.smartattendance.database.AcademicClassDao
import com.swm.smartattendance.database.ShortFormDao
import com.swm.smartattendance.database.SubjectDao
import com.swm.smartattendance.model.ShortForm
import com.swm.smartattendance.model.ShortFormType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val shortFormDao: ShortFormDao,
    private val academicClassDao: AcademicClassDao,
    private val subjectDao: SubjectDao
) : ViewModel() {

    private val _isShortFormEnabled = MutableStateFlow(true)
    val isShortFormEnabled: StateFlow<Boolean> = _isShortFormEnabled

    val classes = academicClassDao.getAllClasses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedClassId = MutableStateFlow<Long?>(null)
    val selectedClassId: StateFlow<Long?> = _selectedClassId

    private val _shortForms = MutableStateFlow<List<ShortForm>>(emptyList())
    val shortForms: StateFlow<List<ShortForm>> = combine(_selectedClassId, _shortForms) { classId, forms ->
        // In a real app, you might filter by class if short forms are class-specific
        // For now, we show all since the DAO doesn't have classId yet
        forms
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshShortForms()
    }

    fun toggleShortForm(enabled: Boolean) {
        _isShortFormEnabled.value = enabled
    }

    fun selectClass(classId: Long?) {
        _selectedClassId.value = classId
    }

    fun refreshShortForms() {
        viewModelScope.launch {
            val subjects = shortFormDao.getByType(ShortFormType.SUBJECT)
            val branches = shortFormDao.getByType(ShortFormType.BRANCH)
            _shortForms.value = branches + subjects
        }
    }

    fun addShortForm(fullName: String, shortForm: String, type: ShortFormType) {
        viewModelScope.launch {
            shortFormDao.insert(ShortForm(fullName = fullName, shortForm = shortForm, type = type, isCustom = true))
            refreshShortForms()
        }
    }

    fun updateShortForm(shortForm: ShortForm) {
        viewModelScope.launch {
            shortFormDao.update(shortForm)
            refreshShortForms()
        }
    }

    fun deleteShortForm(shortForm: ShortForm) {
        viewModelScope.launch {
            shortFormDao.delete(shortForm)
            refreshShortForms()
        }
    }

    class Factory(
        private val shortFormDao: ShortFormDao,
        private val academicClassDao: AcademicClassDao,
        private val subjectDao: SubjectDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(shortFormDao, academicClassDao, subjectDao) as T
    }
}
