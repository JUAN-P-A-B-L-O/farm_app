import { useEffect, useState } from 'react'
import axios from 'axios'
import UserForm from '../../components/user/UserForm'
import { createUser, deleteUser, getAllUsers, updateUser } from '../../services/userService'
import type { User, UserApiErrorResponse, UserFormData } from '../../types/user'
import '../../App.css'

const emptyUserForm: UserFormData = {
  name: '',
}

function getErrorMessage(error: unknown, fallbackMessage: string): string {
  if (axios.isAxiosError<UserApiErrorResponse>(error)) {
    const status = error.response?.status
    const apiMessage = error.response?.data?.error

    if (status === 400) {
      return apiMessage ?? 'Validation error while saving user.'
    }

    if (status === 404) {
      return apiMessage ?? 'User not found.'
    }

    if (status === 409) {
      return apiMessage ?? 'User with this name already exists.'
    }

    if (apiMessage) {
      return apiMessage
    }
  }

  return fallbackMessage
}

function UsersPage() {
  const [users, setUsers] = useState<User[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeletingId, setIsDeletingId] = useState<string | null>(null)
  const [listErrorMessage, setListErrorMessage] = useState('')
  const [formErrorMessage, setFormErrorMessage] = useState('')
  const [editingUserId, setEditingUserId] = useState<string | null>(null)
  const [formInitialValues, setFormInitialValues] = useState<UserFormData>(emptyUserForm)

  async function loadUsers() {
    setIsLoading(true)
    setListErrorMessage('')

    try {
      const data = await getAllUsers()
      setUsers(data)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, 'Unable to load users.'))
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void loadUsers()
  }, [])

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
        getErrorMessage(error, editingUserId ? 'Unable to update user.' : 'Unable to create user.'),
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
    })
  }

  function handleCancelEdit() {
    setEditingUserId(null)
    setFormErrorMessage('')
    setFormInitialValues(emptyUserForm)
  }

  async function handleDelete(id: string) {
    const shouldDelete = window.confirm('Are you sure you want to delete this user?')

    if (!shouldDelete) {
      return
    }

    setIsDeletingId(id)
    setListErrorMessage('')

    try {
      await deleteUser(id)

      if (editingUserId === id) {
        handleCancelEdit()
      }

      await loadUsers()
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, 'Unable to delete user.'))
    } finally {
      setIsDeletingId(null)
    }
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">Access Control</p>
        <h1>User Management</h1>
        <p className="animals-page__description">
          Create, update, and remove system users used across production and feeding records.
        </p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{editingUserId ? 'Update User' : 'Create User'}</h2>
              <p>
                {editingUserId
                  ? 'Edit the selected user using the existing data.'
                  : 'Fill in the user information to create a new record.'}
              </p>
            </div>
          </div>

          <UserForm
            initialValues={formInitialValues}
            onSubmit={handleCreateOrUpdate}
            onCancel={editingUserId ? handleCancelEdit : undefined}
            isSubmitting={isSubmitting}
            submitLabel={editingUserId ? 'Update user' : 'Create user'}
            errorMessage={formErrorMessage}
          />
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header">
            <div>
              <h2>User List</h2>
              <p>Review current users and manage updates or deletions.</p>
            </div>
          </div>

          {isLoading && <p className="animals-page__status">Loading users...</p>}

          {listErrorMessage && (
            <p className="animals-page__status animals-page__status--error">
              {listErrorMessage}
            </p>
          )}

          {!isLoading && !listErrorMessage && users.length === 0 && (
            <p className="animals-page__status">No users found.</p>
          )}

          {!isLoading && !listErrorMessage && users.length > 0 && (
            <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((user) => (
                    <tr key={user.id}>
                      <td>{user.name}</td>
                      <td className="animals-table__actions">
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--secondary"
                          onClick={() => handleEdit(user)}
                          disabled={isSubmitting || isDeletingId === user.id}
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--danger"
                          onClick={() => void handleDelete(user.id)}
                          disabled={isSubmitting || isDeletingId === user.id}
                        >
                          {isDeletingId === user.id ? 'Deleting...' : 'Delete'}
                        </button>
                      </td>
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
