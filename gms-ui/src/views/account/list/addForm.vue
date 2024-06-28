<template>
  <a-modal
    v-model:visible="visible"
    :title="$t('account.list.addForm.title')"
    :ok-loading="loading"
    :mask-closable="false"
    :esc-to-close="false"
    :ok-text="$t('button.submit')"
    :on-before-ok="submitClick"
  >
    <a-form ref="formRef" :model="formData" :rules="rules">
      <a-form-item field="name" :label="$t('account.list.addForm.name')">
        <a-input v-model="formData.name" />
      </a-form-item>
      <a-form-item
        field="password"
        :label="$t('account.list.addForm.password')"
        validate-trigger="blur"
      >
        <a-input-password v-model="formData.password" />
      </a-form-item>
      <a-form-item
        field="checkPassword"
        :label="$t('account.list.addForm.passwordCheck')"
        validate-trigger="blur"
      >
        <a-input-password v-model="formData.checkPassword" />
      </a-form-item>
      <a-form-item
        field="birthday"
        :label="$t('account.list.addForm.birthday')"
      >
        <a-date-picker v-model="formData.birthday" :allow-clear="false" />
      </a-form-item>
      <a-form-item
        field="language"
        :label="$t('account.list.addForm.language')"
      >
        <a-select v-model="formData.language">
          <a-option :value="2">English</a-option>
          <a-option :value="3">中文</a-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
  import { reactive, ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import { addAccount, RegisterForm } from '@/api/account';
  import { Message } from '@arco-design/web-vue';
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const { loading, setLoading } = useLoading(false);
  const visible = ref(false);
  const formData = reactive<RegisterForm>({});
  const rules = {
    name: [
      {
        required: true,
        message: t('account.list.addForm.rules.name.require'),
      },
      {
        minLength: 6,
        message: t('account.list.addForm.rules.name.length'),
      },
    ],
    password: [
      {
        required: true,
        message: t('account.list.addForm.rules.password.require'),
      },
      {
        minLength: 6,
        message: t('account.list.addForm.rules.password.length'),
      },
    ],
    checkPassword: [
      {
        required: true,
        message: t('account.list.addForm.rules.passwordCheck.require'),
      },
      {
        validator: (value: any, cb: any) => {
          if (value !== formData.password) {
            cb(t('account.list.addForm.rules.passwordCheck.same'));
          } else {
            cb();
          }
        },
      },
    ],
    birthday: [
      {
        required: true,
        message: t('account.list.addForm.rules.birthday.require'),
      },
    ],
    language: [
      {
        required: true,
        message: t('account.list.addForm.rules.birthday.require'),
      },
    ],
  };

  const init = () => {
    formData.name = undefined;
    formData.password = undefined;
    formData.checkPassword = undefined;
    formData.birthday = undefined;
    formData.language = 3;

    visible.value = true;
  };
  defineExpose({ init });

  const formRef = ref();
  const emit = defineEmits(['reload']);
  const submitClick = async () => {
    const validError = await formRef.value.validate();
    if (validError) return false;

    setLoading(true);
    try {
      await addAccount(formData);
      Message.success(t('message.success'));
      emit('reload');
      return true;
    } finally {
      setLoading(false);
    }
  };
</script>

<script lang="ts">
  export default {
    name: 'AccountAddForm',
  };
</script>
