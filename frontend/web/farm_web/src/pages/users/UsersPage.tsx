import { useEffect, useState } from 'react'
import axios from 'axios'
import UserForm from '../../components/user/UserForm'
import { useTranslation } from '../../hooks/useTranslation'
import { getAccessibleFarms } from '../../services/farmService'
import { createUser, getAllUsers } from '../../services/userService'
import type { Farm } from '../../types/farm'
import type { User, UserApiErrorResponse, UserFormData } from '../../types/user'
import '../../App.css'

const emptyUserForm: UserFormData = {
  name: '',
  email: '',
  role: '',
  password: '',
  active: true,
  farmIds: [],
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

function UsersPage() {
  const { t } = useTranslation()
  const [users, setUsers] = useState<User[]>([])
  const [farms, setFarms] = useState<Farm[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isLoadingFarms, setIsLoadingFarms] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [listErrorMessage, setListErrorMessage] = useState('')
  const [formErrorMessage, setFormErrorMessage] = useState('')
  const [formInitialValues, setFormInitialValues] = useState<UserFormData>(emptyUserForm)

  async function loadUsers() {
    setIsLoading(true)
    setListErrorMessage('')

    try {
      const data = await getAllUsers()
      setUsers(data)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('accessControl.errors.loadList'), t))
    } finally {
      setIsLoading(false)
    }
  }

  async function loadFarms() {
    setIsLoadingFarms(true)

    try {
      const data = await getAccessibleFarms()
      setFarms(data)
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, t('accessControl.errors.loadFarms'), t))
    } finally {
      setIsLoadingFarms(false)
    }
  }

  useEffect(() => {
    void loadUsers()
    void loadFarms()
  }, [])

  async function handleCreate(data: UserFormData) {
    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      await createUser(data)
      setFormInitialValues(emptyUserForm)
      await loadUsers()
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, t('accessControl.errors.create'), t))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('accessControl.eyebrow')}</p>
        <h1>{t('accessControl.title')}</h1>
        <p className="animals-page__description">
          {t('accessControl.description')}
        </p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{t('accessControl.createTitle')}</h2>
              <p>{t('accessControl.createDescription')}</p>
            </div>
          </div>

          <UserForm
            initialValues={formInitialValues}
            farms={farms}
            isLoadingFarms={isLoadingFarms}
            onSubmit={handleCreate}
            isSubmitting={isSubmitting}
            submitLabel={t('accessControl.submitCreate')}
            errorMessage={formErrorMessage}
          />
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header">
            <div>
              <h2>{t('accessControl.listTitle')}</h2>
              <p>{t('accessControl.listDescription')}</p>
            </div>
          </div>

          {isLoading && <p className="animals-page__status">{t('accessControl.loading')}</p>}

          {listErrorMessage && (
            <p className="animals-page__status animals-page__status--error">
              {listErrorMessage}
            </p>
          )}

          {!isLoading && !listErrorMessage && users.length === 0 && (
            <p className="animals-page__status">{t('accessControl.empty')}</p>
          )}

          {!isLoading && !listErrorMessage && users.length > 0 && (
            <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>{t('accessControl.table.name')}</th>
                    <th>{t('accessControl.table.email')}</th>
                    <th>{t('accessControl.table.role')}</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((user) => (
                    <tr key={user.id}>
                      <td>{user.name}</td>
                      <td>{user.email}</td>
                      <td>{user.role}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </article>
      </section>
    </main>
  )
}

export default UsersPage
