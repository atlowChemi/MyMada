# Invalid Delay caption parity

Row22 is the invalid-input substate, separate from the accepted valid Delay editor.
Native Material1.0.0 sets indicator top padding to zero; Material1.14.0 introduces
`material_helper_text_default_padding_top`. On the measured API37 device this added
11px below the unchanged108px input. The migrated caption also inherited the
bundled activity font instead of the native system caption family.

The scoped error update restores the system sans-serif family and zero indicator
top padding. TextInputLayout still owns error announcement, hint/underline state,
wrapping and validation. The valid editor and preference rules are unchanged.

API37 `AdvancedSettingsTest.invalidDelayCaptionRetainsNativeSpacingAndCancel`
passes: the caption uses the system family, its indicator has no added top gap,
and Cancel retains the original six-second preference.

Evidence: `settings-measured-parity/native-alerts-metrics/` under the private
integration evidence archive. Opened matched `pair-5942-en-delay-empty-error-actions.png`
shows aligned modal/field/error/actions; cursor blink is transient. The paired
IME capture is retained. Earlier failed experiments remain separate.
