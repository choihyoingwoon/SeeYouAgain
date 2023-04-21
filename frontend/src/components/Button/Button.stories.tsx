import React from 'react';
import { Story } from '@storybook/react';
import Button, { ButtonProps } from './index';

export default {
    title: 'Atoms/Button',
    component: Button,
};

const Template: Story<ButtonProps> = args => <Button {...args} />;

export const Default = Template.bind({});

Default.args = {
    buttonType: 'default',
    children: '예은 바보',
};

export const Ghost = Template.bind({});

Ghost.args = {
    buttonType: 'ghost',
    children: '예은 바보 아니다',
};
