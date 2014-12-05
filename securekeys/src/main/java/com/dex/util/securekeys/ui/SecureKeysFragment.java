package com.dex.util.securekeys.ui;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dex.util.securekeys.R;
import com.dex.util.securekeys.utils.LoadFonts;
import com.dex.util.securekeys.utils.SecureKeyUtils;
import com.dexetra.securekeyslib.util.Securekeys;

public class SecureKeysFragment extends SecureKeysBaseFragment implements ViewTreeObserver.OnGlobalLayoutListener,SecureKeysActivity.BackKeyListener {

    TextView mPasswordTextView;
    TextView mPassPhraseTextView;
    EditText mPassPhraseEditText;
    ImageButton mGenerateImageButton;
    CheckBox mMaskCheckBox;
    SeekBar mPassLengthSeekBar;
    TextView mPassLengthTitleTextView;
    TextView mPassLengthTextView;
    CheckBox mNumParamCheckBox;
    CheckBox mCaseParamCheckBox;
    CheckBox mCharParamCheckBox;
    View mPassLengthView;
    View mPassParamView;
    View mPassPhraseView;
    View mPassWordView;
    ImageButton mResetButton;
    private View mRootView;
    private boolean mIsKeyBoardShown = false;
    public static boolean isPasswordShown = false;
    private int mKeyBoardHeight;

    public SecureKeysFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

    private void init(View rootView) {
        mPassLengthView = rootView.findViewById(R.id.rel_securekeys_pass_length);
        mPassLengthSeekBar = (SeekBar) rootView.findViewById(R.id.seek_securekeys_password);
        mPassLengthTitleTextView = (TextView) rootView.findViewById(R.id.txt_securekeys_pass_length_title);
        mPassLengthTextView = (TextView) rootView.findViewById(R.id.txt_securekeys_pass_length);

        mPassParamView = rootView.findViewById(R.id.lin_securekeys_pass_params);
        mNumParamCheckBox = (CheckBox) rootView.findViewById(R.id.chk_securekeys_num);
        mCaseParamCheckBox = (CheckBox) rootView.findViewById(R.id.chk_securekeys_case);
        mCharParamCheckBox = (CheckBox) rootView.findViewById(R.id.chk_securekeys_char);

        mPassPhraseView = rootView.findViewById(R.id.rel_securekeys_passphrase);
        mPassPhraseEditText = (EditText) rootView.findViewById(R.id.edt_securekeys_passphrase);
        mGenerateImageButton = (ImageButton) rootView.findViewById(R.id.imb_securekeys_generate);
        mMaskCheckBox = (CheckBox) rootView.findViewById(R.id.chk_securekeys_mask);

        mPassWordView = rootView.findViewById(R.id.rel_securekeys_password);
        mPassPhraseTextView = (TextView) rootView.findViewById(R.id.txt_securekeys_passphrase);
        mPasswordTextView = (TextView) rootView.findViewById(R.id.txt_securekeys_password);
        mResetButton = (ImageButton) rootView.findViewById(R.id.imb_securekeys_reset);


        mPassLengthView.setVisibility(View.GONE);
        mPassParamView.setVisibility(View.GONE);
        mGenerateImageButton.setEnabled(false);
        mPassLengthTextView.setText(Integer.toString(mPassLengthSeekBar.getProgress()));

    }

    private void initListeners(View view) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(this);
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

        mPassPhraseEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                showParams(hasFocus);
            }
        });

        mPassPhraseEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.KEYCODE_ENTER)
                        || actionId == EditorInfo.IME_ACTION_GO) {
                    mGenerateImageButton.performClick();
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
                mGenerateImageButton.setEnabled(s.toString().trim().length() > 0);
            }
        });

        mGenerateImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passPhrase = mPassPhraseEditText.getText().toString();
                mPassWordView.setVisibility(View.VISIBLE);
                mPassPhraseView.setVisibility(View.GONE);
                mPassPhraseTextView.setText(passPhrase);
                mPasswordTextView.setText(new Securekeys().getPassPhrase(passPhrase,
                        mPassLengthSeekBar.getProgress(), mCaseParamCheckBox.isChecked(),
                        mCharParamCheckBox.isChecked(),
                        mNumParamCheckBox.isChecked()));

                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

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

    }

    private void showParams(boolean showParams) {
        if (showParams) {
            mPassLengthView.setVisibility(View.VISIBLE);
            mPassParamView.setVisibility(View.VISIBLE);
        } else {
            mPassLengthView.setVisibility(View.GONE);
            mPassParamView.setVisibility(View.GONE);
        }
    }

    private void reset() {
        mPassPhraseEditText.setText("");
        mPasswordTextView.setText("");
        mCaseParamCheckBox.setChecked(false);
        mCharParamCheckBox.setChecked(false);
        mNumParamCheckBox.setChecked(false);

        mPassLengthTextView.setText(Integer.toString(5));
        mPassLengthSeekBar.setProgress(5);

        mPassWordView.setVisibility(View.GONE);
        mPassPhraseView.setVisibility(View.VISIBLE);

        isPasswordShown  = false;
    }

    @Override
    public void setTypeface(LoadFonts fonts) {
        if (mRootView != null) {
            mPasswordTextView.setTypeface(fonts.getRegular());
            mPassPhraseTextView.setTypeface(fonts.getRegular());
            mPassPhraseEditText.setTypeface(fonts.getRegular());
            mPassLengthTextView.setTypeface(fonts.getBold());
            mPassLengthTextView.setTypeface(fonts.getBold());
        }
    }

    @Override
    public void onStop() {
        try {
            if(mRootView!=null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    mRootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onGlobalLayout() {
        try {
            Rect rect = new Rect();
            getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int statusBarHeight = rect.top;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int diff = (screenHeight - statusBarHeight)
                    - mRootView.getHeight();
            if (diff > 128) {
                if (!mIsKeyBoardShown) {
                    showParams(true);
                }
                mIsKeyBoardShown = true;
                mKeyBoardHeight = diff;
            }else{
                if (mIsKeyBoardShown) {
                    showParams(false);
                }

                mIsKeyBoardShown = false;
                int rowHeight = SecureKeyUtils.dpToPx(getActivity(), 56);
                if (mKeyBoardHeight == 0)
                    mKeyBoardHeight = 4 * rowHeight;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        reset();
    }
}