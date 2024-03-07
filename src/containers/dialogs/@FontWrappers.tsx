import Dialog from 'react-native-dialog';
import { StyleSheet } from 'react-native';
import type { FC, ComponentProps } from 'react';

type DialogButtonProps = ComponentProps<typeof Dialog.Button>;
type TextProps = ComponentProps<typeof Dialog.Title>;
type DialogInputProps = ComponentProps<typeof Dialog.Input>;

const WrappedButton: FC<DialogButtonProps> = ({ style, ...props }) => <Dialog.Button style={[styles.font, style]} {...props} />;
WrappedButton.displayName = Dialog.Button.displayName;
const WrappedDescription: FC<TextProps> = ({ style, ...props }) => <Dialog.Description style={[styles.font, style]} {...props} />;
WrappedDescription.displayName = Dialog.Description.displayName;
const WrappedInput: FC<DialogInputProps> = ({ style, ...props }) => <Dialog.Input style={[styles.font, style]} {...props} />;
WrappedInput.displayName = Dialog.Input.displayName;
const WrappedTitle: FC<TextProps> = ({ style, ...props }) => <Dialog.Title style={[styles.font, style]} {...props} />;
WrappedTitle.displayName = Dialog.Title.displayName;

const styles = StyleSheet.create({
    font: {
        fontFamily: 'OpenSans',
    },
});

export default {
    Button: WrappedButton,
    Container: Dialog.Container,
    Description: WrappedDescription,
    Input: WrappedInput,
    CodeInput: Dialog.CodeInput,
    Switch: Dialog.Switch,
    Title: WrappedTitle,
};
