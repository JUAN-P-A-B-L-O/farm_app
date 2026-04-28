import { useEffect, useState } from 'react'
import axios from 'axios'
import ExportCsvButton from '../../components/common/ExportCsvButton'
import ListingFiltersBar from '../../components/common/ListingFiltersBar'
import PaginationControls from '../../components/common/PaginationControls'
import UserForm from '../../components/user/UserForm'
import { USER_ROLES, getUserActiveLabel, getUserRoleLabel } from '../../i18n/domainLabels'
import { useTranslation } from '../../hooks/useTranslation'
import { getAccessibleFarms } from '../../services/farmService'
import {
  activateUser,
  createUser,
  deleteUser,
  exportUsersCsv,
  getUsersPage,
  inactivateUser,
  updateUser,
} from '../../services/userService'
import type { Farm } from '../../types/farm'
import type { User, UserApiErrorResponse, UserFormData, UserListFilters } from '../../types/user'
import { createEmptyPaginatedResponse, DEFAULT_PAGE_SIZE } from '../../utils/pagination'
import '../../App.css'

const emptyUserForm: UserFormData = {
  name: '',
  email: '',
  role: '',
  password: '',
  active: true,
  avatarUrl: '',
  farmIds: [],
}

const defaultFilters: UserListFilters = {
  search: '',
  active: '',
  role: '',
}

function getErrorMessage(error: unknown, fallbackMessage: string, t: (key: string) => string): string {
  if (axios.isAxiosError<UserApiErrorResponse>(error)) {
    const status = error.response?.status
    const apiMessage = error.response?.data?.error

    if (status === 400) {
      return apiMessage ?? t('accessControl.errors.validationSave')
    }

    if (status === 404) {
      return apiMessage ?? t('accessControl.errors.notFound')
    }

    if (status === 409) {
      return apiMessage ?? t('accessControl.errors.duplicateEmail')
    }

    if (apiMessage) {
      return apiMessage
    }
  }

  return fallbackMessage
}

function getInitials(user: User) {
  return user.name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('')
}

function UsersPage() {
  const { t } = useTranslation()
  const [users, setUsers] = useState<User[]>([])
  const [pagination, setPagination] = useState(createEmptyPaginatedResponse<User>())
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE)
  const [farms, setFarms] = useState<Farm[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isLoadingFarms, setIsLoadingFarms] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isMutatingUserId, setIsMutatingUserId] = useState<string | null>(null)
  const [listErrorMessage, setListErrorMessage] = useState('')
  const [formErrorMessage, setFormErrorMessage] = useState('')
  const [editingUserId, setEditingUserId] = useState<string | null>(null)
  const [formInitialValues, setFormInitialValues] = useState<UserFormData>(emptyUserForm)
  const [filters, setFilters] = useState<UserListFilters>(defaultFilters)
  const [appliedFilters, setAppliedFilters] = useState<UserListFilters>(defaultFilters)
  const [activationUserId, setActivationUserId] = useState<string | null>(null)
  const [activationPassword, setActivationPassword] = useState('')
  const [isExporting, setIsExporting] = useState(false)

  async function loadUsers(
    nextFilters: UserListFilters = appliedFilters,
    targetPage = page,
    targetSize = pageSize,
  ) {
    setIsLoading(true)
    setListErrorMessage('')

    try {
      const data = await getUsersPage(nextFilters, { page: targetPage, size: targetSize })

      if (data.content.length === 0 && data.totalElements > 0 && data.totalPages > 0 && targetPage >= data.totalPages) {
        await loadUsers(nextFilters, data.totalPages - 1, targetSize)
        return
      }

      setUsers(data.content)
      setPagination(data)
      setPage(data.page)
      setPageSize(data.size)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('accessControl.errors.loadList'), t))
    } finally {
      setIsLoading(false)
    }
  }

  async function loadFarms() {
    setIsLoadingFarms(true)

    try {
      const data = await getAccessibleFarms({ ownedOnly: true })
      setFarms(data)
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, t('accessControl.errors.loadFarms'), t))
    } finally {
      setIsLoadingFarms(false)
    }
  }

  useEffect(() => {
    void loadUsers(defaultFilters, 0, pageSize)
    void loadFarms()
  }, [])

  function handlePageChange(nextPage: number) {
    if (nextPage === page) {
      return
    }

    void loadUsers(appliedFilters, nextPage, pageSize)
  }

  function handlePageSizeChange(nextSize: number) {
    setPage(0)
    setPageSize(nextSize)
    void loadUsers(appliedFilters, 0, nextSize)
  }

  async function handleCreateOrUpdate(data: UserFormData) {
    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      if (editingUserId) {
        await updateUser(editingUserId, data)
      } else {
        await createUser(data)
      }
      setEditingUserId(null)
      setFormInitialValues(emptyUserForm)
      await loadUsers()
    } catch (error) {
      setFormErrorMessage(
        getErrorMessage(
          error,
          editingUserId ? t('accessControl.errors.update') : t('accessControl.errors.create'),
          t,
        ),
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  function handleEdit(user: User) {
    setFormErrorMessage('')
    setEditingUserId(user.id)
    setFormInitialValues({
      name: user.name,
      email: user.email,
      role: user.role,
      password: '',
      active: user.active,
      avatarUrl: user.avatarUrl ?? '',
      farmIds: user.farmIds,
    })
  }

  function handleCancelEdit() {
    setEditingUserId(null)
    setFormErrorMessage('')
    setFormInitialValues(emptyUserForm)
  }

  async function handleInactivate(user: User) {
    const shouldInactivate = window.confirm(t('accessControl.confirmInactivate'))

    if (!shouldInactivate) {
      return
    }

    setIsMutatingUserId(user.id)
    setListErrorMessage('')

    try {
      await inactivateUser(user.id)

      if (editingUserId === user.id) {
        handleCancelEdit()
      }
      if (activationUserId === user.id) {
        setActivationUserId(null)
        setActivationPassword('')
      }

      await loadUsers()
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('accessControl.errors.inactivate'), t))
    } finally {
      setIsMutatingUserId(null)
    }
  }

  function startActivation(user: User) {
    setActivationUserId(user.id)
    setActivationPassword('')
    setListErrorMessage('')
  }

  function cancelActivation() {
    setActivationUserId(null)
    setActivationPassword('')
  }

  async function handleActivate() {
    if (!activationUserId) {
      return
    }

    if (!activationPassword.trim()) {
      setListErrorMessage(t('accessControl.errors.activatePasswordRequired'))
      return
    }

    setIsMutatingUserId(activationUserId)
    setListErrorMessage('')

    try {
      await activateUser(activationUserId, activationPassword.trim())
      cancelActivation()
      await loadUsers()
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('accessControl.errors.activate'), t))
    } finally {
      setIsMutatingUserId(null)
    }
  }

  async function handleDelete(user: User) {
    const shouldDelete = window.confirm(t('accessControl.confirmDelete'))

    if (!shouldDelete) {
      return
    }

    setIsMutatingUserId(user.id)
    setListErrorMessage('')

    try {
      await deleteUser(user.id)

      if (editingUserId === user.id) {
        handleCancelEdit()
      }
      if (activationUserId === user.id) {
        cancelActivation()
      }

      await loadUsers()
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('accessControl.errors.delete'), t))
    } finally {
      setIsMutatingUserId(null)
    }
  }

  function applyFilters() {
    setAppliedFilters(filters)
    setPage(0)
    void loadUsers(filters, 0, pageSize)
  }

  function clearFilters() {
    setFilters(defaultFilters)
    setAppliedFilters(defaultFilters)
    setPage(0)
    void loadUsers(defaultFilters, 0, pageSize)
  }

  async function handleExport() {
    setIsExporting(true)
    setListErrorMessage('')

    try {
      await exportUsersCsv(appliedFilters)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('common.exportError'), t))
    } finally {
      setIsExporting(false)
    }
  }

  function resolveFarmNames(farmIds: string[]) {
    return farmIds
      .map((farmId) => farms.find((farm) => farm.id === farmId)?.name ?? farmId)
      .join(', ')
  }

  const activationTarget = users.find((user) => user.id === activationUserId) ?? null

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('accessControl.eyebrow')}</p>
        <h1>{t('accessControl.title')}</h1>
        <p className="animals-page__description">{t('accessControl.description')}</p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{editingUserId ? t('accessControl.updateTitle') : t('accessControl.createTitle')}</h2>
              <p>
                {editingUserId
                  ? t('accessControl.updateDescription')
                  : t('accessControl.createDescription')}
              </p>
            </div>
          </div>

          <UserForm
            initialValues={formInitialValues}
            farms={farms}
            isLoadingFarms={isLoadingFarms}
            mode={editingUserId ? 'edit' : 'create'}
            onSubmit={handleCreateOrUpdate}
            onCancel={editingUserId ? handleCancelEdit : undefined}
            isSubmitting={isSubmitting}
            submitLabel={editingUserId ? t('accessControl.submitUpdate') : t('accessControl.submitCreate')}
            errorMessage={formErrorMessage}
          />
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header animals-panel__header--actions">
            <div>
              <h2>{t('accessControl.listTitle')}</h2>
              <p>{t('accessControl.listDescription')}</p>
            </div>
            <ExportCsvButton
              onClick={() => void handleExport()}
              label={t('common.exportCsv')}
              loadingLabel={t('common.exportingCsv')}
              isLoading={isExporting}
              disabled={isLoading || users.length === 0}
            />
          </div>

          <ListingFiltersBar
            search={{
              id: 'users-search',
              label: t('accessControl.filters.searchLabel'),
              placeholder: t('accessControl.filters.searchPlaceholder'),
              value: filters.search,
              onChange: (value) => setFilters((current) => ({ ...current, search: value })),
            }}
            onApply={applyFilters}
            onClear={clearFilters}
            applyLabel={t('accessControl.filters.apply')}
            clearLabel={t('accessControl.filters.clear')}
            filters={[
              {
                type: 'select',
                id: 'users-status-filter',
                label: t('accessControl.filters.statusLabel'),
                value: filters.active,
                onChange: (value) => setFilters((current) => ({ ...current, active: value as UserListFilters['active'] })),
                options: [
                  { value: '', label: t('accessControl.filters.allStatuses') },
                  { value: 'true', label: t('accessControl.status.active') },
                  { value: 'false', label: t('accessControl.status.inactive') },
                ],
              },
              {
                type: 'select',
                id: 'users-role-filter',
                label: t('accessControl.filters.roleLabel'),
                value: filters.role,
                onChange: (value) => setFilters((current) => ({ ...current, role: value as UserListFilters['role'] })),
                options: [
                  { value: '', label: t('accessControl.filters.allRoles') },
                  ...USER_ROLES.map((role) => ({
                    value: role,
                    label: getUserRoleLabel(t, role),
                  })),
                ],
              },
            ]}
          />

          {activationTarget && (
            <div className="activation-panel">
              <div>
                <h3>{t('accessControl.activateTitle')}</h3>
                <p>{t('accessControl.activateDescription', { name: activationTarget.name })}</p>
              </div>
              <label className="animal-form__field" htmlFor="activation-password">
                <span>{t('accessControl.form.password')}</span>
                <input
                  id="activation-password"
                  type="password"
                  value={activationPassword}
                  onChange={(event) => setActivationPassword(event.target.value)}
                  placeholder={t('accessControl.form.placeholders.password')}
                />
              </label>
              <div className="animal-form__actions">
                <button
                  type="button"
                  className="animals-table__action-button"
                  onClick={() => void handleActivate()}
                  disabled={isMutatingUserId === activationTarget.id}
                >
                  {isMutatingUserId === activationTarget.id
                    ? t('accessControl.activating')
                    : t('accessControl.activate')}
                </button>
                <button
                  type="button"
                  className="animals-table__action-button animals-table__action-button--secondary"
                  onClick={cancelActivation}
                  disabled={isMutatingUserId === activationTarget.id}
                >
                  {t('common.cancel')}
                </button>
              </div>
            </div>
          )}

          {isLoading && <p className="animals-page__status">{t('accessControl.loading')}</p>}

          {listErrorMessage && (
            <p className="animals-page__status animals-page__status--error">{listErrorMessage}</p>
          )}

          {!isLoading && !listErrorMessage && users.length === 0 && (
            <p className="animals-page__status">{t('accessControl.empty')}</p>
          )}

          {!isLoading && !listErrorMessage && users.length > 0 && (
            <>
              <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>{t('accessControl.table.name')}</th>
                    <th>{t('accessControl.table.email')}</th>
                    <th>{t('accessControl.table.role')}</th>
                    <th>{t('accessControl.table.status')}</th>
                    <th>{t('accessControl.table.farms')}</th>
                    <th>{t('accessControl.table.actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((user) => (
                    <tr key={user.id}>
                      <td>
                        <div className="user-table__identity">
                          {user.avatarUrl ? (
                            <img src={user.avatarUrl} alt={user.name} className="user-avatar" />
                          ) : (
                            <span className="user-avatar user-avatar--fallback">{getInitials(user)}</span>
                          )}
                          <span>{user.name}</span>
                        </div>
                      </td>
                      <td>{user.email}</td>
                      <td>{getUserRoleLabel(t, user.role)}</td>
                      <td>
                        <span className={`animals-table__status animals-table__status--${user.active ? 'active' : 'inactive'}`}>
                          {getUserActiveLabel(t, user.active)}
                        </span>
                      </td>
                      <td>{resolveFarmNames(user.farmIds)}</td>
                      <td className="animals-table__actions">
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--secondary"
                          onClick={() => handleEdit(user)}
                          disabled={isSubmitting || isMutatingUserId === user.id}
                        >
                          {t('accessControl.edit')}
                        </button>
                        {user.active ? (
                          <button
                            type="button"
                            className="animals-table__action-button animals-table__action-button--secondary"
                            onClick={() => void handleInactivate(user)}
                            disabled={isSubmitting || isMutatingUserId === user.id}
                          >
                            {isMutatingUserId === user.id
                              ? t('accessControl.inactivating')
                              : t('accessControl.inactivate')}
                          </button>
                        ) : (
                          <button
                            type="button"
                            className="animals-table__action-button"
                            onClick={() => startActivation(user)}
                            disabled={isSubmitting || isMutatingUserId === user.id}
                          >
                            {isMutatingUserId === user.id
                              ? t('accessControl.activating')
                              : t('accessControl.activate')}
                          </button>
                        )}
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--danger"
                          onClick={() => void handleDelete(user)}
                          disabled={isSubmitting || isMutatingUserId === user.id}
                        >
                          {isMutatingUserId === user.id
                            ? t('accessControl.deleting')
                            : t('accessControl.delete')}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              </div>

              <PaginationControls
                pagination={pagination}
                isLoading={isLoading}
                onPageChange={handlePageChange}
                onPageSizeChange={handlePageSizeChange}
              />
            </>
          )}
        </article>
      </section>
    </main>
  )
}

export default UsersPage
