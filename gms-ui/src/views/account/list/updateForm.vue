<template>
  <a-modal
    v-model:visible="visible"
    :title="title"
    :ok-loading="loading"
    :mask-closable="false"
    :esc-to-close="false"
    :ok-text="$t('button.submit')"
    :on-before-ok="submitClick"
  >
    <a-form ref="formRef" :rules="rules" :model="formData">
      <a-form-item
        field="newPwd"
        :label="$t('account.list.updateForm.password')"
        validate-trigger="blur"
      >
        <a-input-password
          v-model="formData.newPwd"
          :placeholder="$t('account.list.updateForm.password.placeholder')"
        />
      </a-form-item>
      <a-form-item
        field="newPwdCheck"
        :label="$t('account.list.updateForm.passwordCheck')"
        validate-trigger="blur"
      >
        <a-input-password v-model="formData.newPwdCheck" />
      </a-form-item>
      <a-form-item
        field="pin"
        :label="$t('account.list.updateForm.pin')"
        validate-trigger="blur"
      >
        <a-input-password v-model="formData.pin" />
      </a-form-item>
      <a-form-item
        field="pic"
        :label="$t('account.list.updateForm.pic')"
        validate-trigger="blur"
      >
        <a-input-password v-model="formData.pic" />
      </a-form-item>
      <a-form-item
        field="birthday"
        :label="$t('account.list.updateForm.birthday')"
      >
        <a-date-picker v-model="formData.birthday" :allow-clear="false" />
      </a-form-item>
      <a-form-item
        field="nxCredit"
        :label="$t('account.list.updateForm.nxCredit')"
        validate-trigger="blur"
      >
        <a-input-number v-model="formData.nxCredit" />
      </a-form-item>
      <a-form-item
        field="nxPrepaid"
        :label="$t('account.list.updateForm.nxPrepaid')"
        validate-trigger="blur"
      >
        <a-input-number v-model="formData.nxPrepaid" />
      </a-form-item>
      <a-form-item
        field="maplePoint"
        :label="$t('account.list.updateForm.maplePoint')"
        validate-trigger="blur"
      >
        <a-input-number v-model="formData.maplePoint" />
      </a-form-item>
      <a-form-item
        field="characterslots"
        :label="$t('account.list.updateForm.characterSlots')"
        validate-trigger="blur"
      >
        <a-input-number v-model="formData.characterslots" />
      </a-form-item>
      <a-form-item field="gender" :label="$t('account.list.updateForm.gender')">
        <a-select v-model="formData.gender">
          <a-option :value="0">
            {{ $t('account.list.updateForm.gender.male') }}
          </a-option>
          <a-option :value="1">
            {{ $t('account.list.updateForm.gender.female') }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        field="webadmin"
        :label="$t('account.list.updateForm.webadmin')"
      >
        <a-select v-model="formData.webadmin">
          <a-option :value="0">
            {{ $t('account.list.updateForm.no') }}
          </a-option>
          <a-option :value="1">
            {{ $t('account.list.updateForm.yes') }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        field="nick"
        :label="$t('account.list.updateForm.nick')"
        validate-trigger="blur"
      >
        <a-input v-model="formData.nick" />
      </a-form-item>
      <a-form-item field="mute" :label="$t('account.list.updateForm.mute')">
        <a-select v-model="formData.mute">
          <a-option :value="0">
            {{ $t('account.list.updateForm.no') }}
          </a-option>
          <a-option :value="1">
            {{ $t('account.list.updateForm.yes') }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        field="email"
        :label="$t('account.list.updateForm.email')"
        validate-trigger="blur"
      >
        <a-input v-model="formData.email" />
      </a-form-item>
      <a-form-item
        field="rewardpoints"
        :label="$t('account.list.updateForm.rewardpoints')"
        validate-trigger="blur"
      >
        <a-input-number v-model="formData.rewardpoints" />
      </a-form-item>
      <a-form-item
        field="votepoints"
        :label="$t('account.list.updateForm.votepoints')"
        validate-trigger="blur"
      >
        <a-input-number v-model="formData.votepoints" />
      </a-form-item>
      <a-form-item
        field="language"
        :label="$t('account.list.updateForm.language')"
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
  import { AccountState } from '@/store/modules/account/types';
  import { GMUpdateForm, updateAccountByGM } from '@/api/account';
  import { Message } from '@arco-design/web-vue';
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const { loading, setLoading } = useLoading(false);
  const visible = ref(false);
  const title = ref('');
  const formData = reactive<GMUpdateForm>({});
  const id = ref<number>(0);
  const rules = {
    newPwd: [
      {
        minLength: 6,
        message: t('account.list.updateForm.rules.password.length'),
      },
    ],
    newPwdCheck: [
      {
        validator: (value: any, cb: any) => {
          if (
            value === formData.newPwd ||
            (formData.newPwd === '' && value === undefined)
          ) {
            cb();
          } else {
            cb(t('account.list.updateForm.rules.passwordCheck.same'));
          }
        },
      },
    ],
    birthday: [
      {
        required: true,
        message: t('account.list.updateForm.rules.birthday.require'),
      },
    ],
    pin: [
      {
        validator: (value: any, cb: any) => {
          if (value === '' || /^\d{4}$/.test(value)) {
            cb();
          } else {
            cb(t('account.list.updateForm.rules.pin.length'));
          }
        },
      },
    ],
    pic: [
      {
        validator: (value: any, cb: any) => {
          if (value === '' || /^\d{6}$/.test(value)) {
            cb();
          } else {
            cb(t('account.list.updateForm.rules.pic.length'));
          }
        },
      },
    ],
    language: [
      {
        required: true,
        message: t('account.list.addForm.rules.language.require'),
      },
    ],
  };

  const init = (accountData: AccountState) => {
    id.value = accountData.id;
    formData.newPwd = undefined;
    formData.newPwdCheck = undefined;
    formData.pin = accountData.pin;
    formData.pic = accountData.pic;
    formData.birthday = accountData.birthday;
    formData.nxCredit = accountData.nxCredit;
    formData.maplePoint = accountData.maplePoint;
    formData.nxPrepaid = accountData.nxPrepaid;
    formData.characterslots = accountData.characterslots;
    formData.gender = accountData.gender;
    formData.webadmin = accountData.webadmin;
    formData.nick = accountData.nick;
    formData.mute = accountData.mute;
    formData.email = accountData.email;
    formData.rewardpoints = accountData.rewardpoints;
    formData.votepoints = accountData.votepoints;
    formData.language = accountData.language;

    title.value = `${t('account.list.updateForm.title')} [${id.value}] ${
      accountData.name
    }`;

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
      await updateAccountByGM(id.value, formData);
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
    name: 'AccountUpdateForm',
  };
</script>
