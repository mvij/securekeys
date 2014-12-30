package com.dexetra.securekeys.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.SubscriptSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dexetra.securekeys.R;
import com.dexetra.securekeys.utils.LoadFonts;
import com.dexetra.securekeys.utils.ThemeHelper;
import com.dexetra.securekeys.widgets.MeasuredTextView;
import com.dexetra.securekeyslib.util.Securekeys;

public class SecureKeysFragment extends SecureKeysBaseFragment implements SecureKeysActivity.BackKeyListener {

    private static final String CHAR = "char";
    private static final String CASE = "case";
    private static final String NUM = "num";
    private static final String LENGTH = "length";
    View mPasswordView;
    MeasuredTextView mPasswordTextView;
    //TextView mPassPhraseTextView;
    EditText mPassPhraseEditText;
    Button mGenerateButton;
    CheckBox mMaskCheckBox;
    SeekBar mPassLengthSeekBar;
    //TextView mPassLengthTitleTextView;
    TextView mPassLengthTextView;
    CheckBox mNumParamCheckBox;
    CheckBox mCaseParamCheckBox;
    CheckBox mCharParamCheckBox;
    TextView mCaseLabel;
    TextView mNumLabel;
    TextView mCharLabel;
    TextView mCaseLabel1;
    TextView mNumLabel1;
    TextView mCharLabel1;
    TextView mPassLabel;
    TextView mPasswordHint;
    //View mPassParamView;
    //View mPassPhraseView;
    //View mPassWordView;
    //ImageButton mResetButton;
    private View mRootView;
    private boolean mIsKeyBoardShown = false;
    public static boolean isPasswordShown = false;
    private int mKeyBoardHeight;

    private int normalColor,accentColor;
    private ThemeHelper mThemeHelper;

    public SecureKeysFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mRootView = inflater.inflate(R.layout.fragment_securekeys, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view != null) {
            init(view);
            initListeners(view);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.secure_keys, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItem = menu.findItem(R.id.action_theme);
        if(menuItem!=null){
            menuItem.setTitle(mThemeHelper.getCurrentTheme().name().equals(ThemeHelper.Theme.DARK.name())?"Light Theme":"Dark Theme");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_help:
                Intent intent = WebviewActivity.getLaunchIntent(getActivity());
                startActivity(intent);
                break;
            case R.id.action_theme:
                mThemeHelper.toggleTheme();
                ThemeHelper.changeToTheme(getActivity());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setElevation(0);
        doUpTheme();
    }

    private void doUpTheme() {
        mThemeHelper = ThemeHelper.instance(getActivity());
        ThemeHelper.Theme theme = mThemeHelper.getCurrentTheme();
        normalColor = getResources().getColor(theme.normalColorId);
        accentColor = getResources().getColor(theme.accentColorId);

        getActivity().setTheme(mThemeHelper.getCurrentTheme().equals(ThemeHelper.Theme.LIGHT)?R.style.SecureKeys_Dark:R.style.SecureKeys_Light);

        ActionBar actionBar =((ActionBarActivity)getActivity()).getSupportActionBar();
        actionBar.setIcon(theme.iconResId);

        getView().findViewById(R.id.rootview).setBackgroundResource(theme.bgColorId);

        mPassPhraseEditText.setBackgroundResource(theme.edtBgColorId);

        mCaseLabel.setBackgroundResource(theme.edtBgColorId);
        mNumLabel.setBackgroundResource(theme.edtBgColorId);
        mCharLabel.setBackgroundResource(theme.edtBgColorId);

        mPassLengthSeekBar.setProgressDrawable(getResources().getDrawable(theme.seekbarResId));
        mPassLengthSeekBar.setThumb(getResources().getDrawable(theme.seekbarThumbId));

        mGenerateButton.setBackgroundResource(theme.genButtonResId);

        mPasswordTextView.setColorResId(theme.accentColorId,theme.shadowColorId);

        mPasswordView.setBackgroundResource(theme.passwordBgColorId);
    }

    private void init(View rootView) {
        //mPassLengthView = rootView.findViewById(R.id.rel_securekeys_pass_length);
        mPassLengthSeekBar = (SeekBar) rootView.findViewById(R.id.seek_securekeys_password);
        mPassLengthTextView = (TextView) rootView.findViewById(R.id.txt_securekeys_pas_length);
        //mPassLengthTextView = (TextView) rootView.findViewById(R.id.txt_securekeys_pass_length);

        mNumLabel = (TextView)rootView.findViewById(R.id.txt_securekeys_num_label);
        mNumLabel1 = (TextView)rootView.findViewById(R.id.txt_securekeys_num_label1);
        mCharLabel = (TextView)rootView.findViewById(R.id.txt_securekeys_char_label);
        mCharLabel1 = (TextView)rootView.findViewById(R.id.txt_securekeys_char_label1);
        mCaseLabel = (TextView)rootView.findViewById(R.id.txt_securekeys_case_label);
        mCaseLabel1 = (TextView)rootView.findViewById(R.id.txt_securekeys_case_label1);

        mNumParamCheckBox = (CheckBox) rootView.findViewById(R.id.chk_securekeys_num);
        mCaseParamCheckBox = (CheckBox) rootView.findViewById(R.id.chk_securekeys_case);
        mCharParamCheckBox = (CheckBox) rootView.findViewById(R.id.chk_securekeys_char);

        //mPassPhraseView = rootView.findViewById(R.id.rel_securekeys_passphrase);
        mPassPhraseEditText = (EditText) rootView.findViewById(R.id.edt_securekeys_passphrase);
        mGenerateButton = (Button) rootView.findViewById(R.id.imb_securekeys_generate);
        mMaskCheckBox = (CheckBox) rootView.findViewById(R.id.chk_securekeys_mask);

        mPasswordView = rootView.findViewById(R.id.lin_securekeys_generated);
        mPasswordHint = (TextView)rootView.findViewById(R.id.txt_securekeys_password_hint);
        mPassLabel = (TextView)rootView.findViewById(R.id.txt_securekeys_password_label);
        mPasswordTextView = (MeasuredTextView) rootView.findViewById(R.id.txt_securekeys_password);
        //mResetButton = (ImageButton) rootView.findViewById(R.id.imb_securekeys_reset);


        //mPassLengthView.setVisibility(View.GONE);
        mGenerateButton.setEnabled(false);
        //mPassLengthTextView.setText(Integer.toString(mPassLengthSeekBar.getProgress()));

    }

    private void initListeners(View view) {
        mPasswordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        ClipboardManager clipboard =
                                (ClipboardManager) getActivity().getSystemService(
                                        Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("secure",
                                mPasswordTextView.getText());
                        clipboard.setPrimaryClip(clip);
                    } else {
                        android.text.ClipboardManager clipboard =
                                (android.text.ClipboardManager) getActivity().getSystemService(
                                        Context.CLIPBOARD_SERVICE);
                        clipboard.setText(mPasswordTextView.getText());

                    }
                    Toast.makeText(getActivity(), R.string.password_copied, Toast.LENGTH_LONG)
                         .show();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        mMaskCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    if (isChecked) {
                        mPassPhraseEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    } else {
                        mPassPhraseEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    }
                }
            }
        });

        mPassPhraseEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.KEYCODE_ENTER)
                        || actionId == EditorInfo.IME_ACTION_GO) {
                    mGenerateButton.performClick();
                }
                return false;
            }
        });

        mPassPhraseEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mGenerateButton.setEnabled(s.toString().trim().length() > 0);
            }
        });

        mGenerateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((ActionBarActivity)getActivity()).getSupportActionBar().hide();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String passPhrase = mPassPhraseEditText.getText().toString();
                mPasswordView.setVisibility(View.VISIBLE);
                //mPassPhraseView.setVisibility(View.GONE);
                //mPassPhraseTextView.setText(passPhrase);

                mPasswordTextView.setText(new Securekeys().getPassPhrase(passPhrase,
                        mPassLengthSeekBar.getProgress(), mCaseParamCheckBox.isChecked(),
                        mCharParamCheckBox.isChecked(), mNumParamCheckBox.isChecked()));

                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                isPasswordShown = true;
            }
        });

        mPassLengthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                    return;
                mPassLengthTextView.setText(Integer.toString(progress));
                if (progress < 5) {
                    mPassLengthTextView.setText(Integer.toString(5));
                    seekBar.setProgress(5);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //mResetButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        reset();
        //    }
        //});

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            mPasswordTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(mPasswordTextView.getText());
                    Toast.makeText(
                            getActivity(),
                            R.string.password_copied,
                            Toast.LENGTH_LONG).show();
                    return true;
                }
            });

        mCaseParamCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mCaseLabel.setTextColor(isChecked?accentColor:normalColor);
                    mCaseLabel1.setTextColor(isChecked ? accentColor : normalColor);
            }
        });

        mCharParamCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mCharLabel.setTextColor(isChecked?accentColor:normalColor);
                    mCharLabel1.setTextColor(isChecked ? accentColor : normalColor);
            }
        });
        mNumParamCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mNumLabel.setTextColor(isChecked?accentColor:normalColor);
                    mNumLabel1.setTextColor(isChecked ? accentColor : normalColor);
            }
        });

        mCharLabel.setText(correctedCharText());
    }

    private CharSequence correctedCharText() {
        SpannableStringBuilder s = new SpannableStringBuilder();
        s.append("#*");
        s.setSpan(new SubscriptSpan(),1,s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }


    private void reset() {
        try {
            ((ActionBarActivity)getActivity()).getSupportActionBar().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPassPhraseEditText.setText("");
        mPasswordTextView.setText(null);
        mCaseParamCheckBox.setChecked(false);
        mCharParamCheckBox.setChecked(false);
        mNumParamCheckBox.setChecked(false);

        mPassLengthTextView.setText(Integer.toString(5));
        mPassLengthSeekBar.setProgress(5);

        mPasswordView.setVisibility(View.GONE);
        //mPassPhraseView.setVisibility(View.VISIBLE);

        isPasswordShown  = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LENGTH,mPassLengthSeekBar.getProgress());
        outState.putBoolean(CHAR,mCharParamCheckBox.isChecked());
        outState.putBoolean(CASE,mCaseParamCheckBox.isChecked());
        outState.putBoolean(NUM,mNumParamCheckBox.isChecked());
    }

    @Override
    public void setTypeface(LoadFonts fonts) {
        if (mRootView != null) {
            mPassPhraseEditText.setTypeface(fonts.getRegular());
            mPassLengthTextView.setTypeface(fonts.getBold());

            mCaseLabel.setTypeface(fonts.getBold());
            mNumLabel.setTypeface(fonts.getBold());
            mCharLabel.setTypeface(fonts.getBold());

            mCaseLabel1.setTypeface(fonts.getLight());
            mCharLabel1.setTypeface(fonts.getLight());
            mNumLabel1.setTypeface(fonts.getLight());

            mGenerateButton.setTypeface(fonts.getLight());

            mMaskCheckBox.setTypeface(fonts.getLight());

            mPasswordHint.setTypeface(fonts.getLight());
            mPassLabel.setTypeface(fonts.getRegular());
            mPasswordTextView.setTypeface(fonts.getBold());
        }
    }

    private void clearPasswordFromClipboard() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB){
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(
                    Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("secure", "");
            clipboard.setPrimaryClip(clip);
        }else {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(
                    Context.CLIPBOARD_SERVICE);
            clipboard.setText("");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearPasswordFromClipboard();
    }

    @Override
    public void onBackPressed() {
        reset();
    }
}