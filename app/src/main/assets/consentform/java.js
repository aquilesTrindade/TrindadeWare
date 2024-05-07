    var obj = {};
    var state = 'init';
    var consentDelayMs = 1000;
     // Function that opens app privacy policy URL in a browser.
    var showAppPrivacyPolicy = function() {};

    function getStyle(el, prop, notParseToNumber) {
      var style = window.getComputedStyle(el, null);
      var val;
      if (prop === undefined) {
        return style;
      } else {
        val = style.getPropertyValue(prop);
        if (val === undefined || val === null || val === '') {
          return;
        }
        return notParseToNumber ? val : parseFloat(val);
      }
    };

    function hideAllConsents() {
      var consentList = document.getElementsByClassName('consent')
      var i = consentList.length;
      while (i-- > 0) {
        consentList[i].classList.add('no-display');
        consentList[i].classList.remove('fade-in');
      }
    };

    function consentAnimation(consentEl, to) {
      var contentEl = obj.contentEl;
      var contentBoxEl = obj.contentBoxEl;
      var contentElPadding = 2 * getStyle(contentEl, 'padding-top');
      var optionsEl = obj.optionsEl;
      var newH;

      // set the height with number (instead of 'auto') to get ready for animation
      contentEl.style.height = contentBoxEl.offsetHeight + contentElPadding;
      if (to === 'in') {
        optionsEl.classList.add('no-display');
        optionsEl.classList.remove('fade-in');
        consentEl.classList.remove('no-display');
        consentEl.style.opacity = 0;
      } else if (to === 'out') {
        consentEl.classList.add('no-display');
        consentEl.classList.remove('fade-in');
        optionsEl.classList.remove('no-display');
        optionsEl.style.opacity = 0;
      }

      setTimeout(function() {
        if (to === 'in') {
          // fade in the detail info
          consentEl.classList.add('fade-in');
        } else if (to === 'out') {
          // fade in the options
          optionsEl.classList.add('fade-in');
        }
        // make animation occurs towards the new height
        newH = contentBoxEl.offsetHeight + contentElPadding;
        contentEl.style.height =  newH % 2 === 0 ? newH : newH + 1;
      }, 0)
    };

    function showProviders() {
      consentAnimation(obj.consent0El, 'in');
      document.getElementById('providersList').scrollTop = 0;
    };

    function showUniqueIdInfo() {
      consentAnimation(obj.consent1El, 'in');
    };

    function actionClick(e, id) {
      switch (id) {
        case 'close':
          // 'Close App'
          dismissConsentModal('close_app');
          break;
        case 'back0':
          consentAnimation(obj.consent0El, 'out');
          break;
        case 'back1':
          consentAnimation(obj.consent1El, 'out');
          break;
        case 'agree0':
          // 'Agree' on 'See personalized ads'
          dismissConsentModal('personalized');
          break;
        case 'agree1':
          // 'Agree' on 'See non-personalized ads'
          dismissConsentModal('non_personalized');
          break;
        case 'install':
          // 'Install' on 'See no ads'
          dismissConsentModal('ad_free');
          break;
      }
    };

    function dismissConsentModal(status) {
      var info = {
        'action': 'dismiss',
        'status': status || 'Error: no status.'
      };
      sendMessage(info);
    };

    function formLoadCompleted(status) {
      var info = {
        'action': 'load_complete',
        'status': status || 'Error: no status.'
      };
      sendMessage(info);
    };

    function openBrowser(url) {
      var info = {
        'action': 'browser',
        'url': url || ''
      }
      sendMessage(info);
      // Return false to prevent default click handling.
      return false;
    }

    function sendMessage(info) {
      function queryStringFromDictionary(d) {
        var params = [];
        for(var k in d) {
          params.push(encodeURIComponent(k) + '=' + encodeURIComponent(d[k]));
        }
        return params.join('&');
      };
      var iframe = document.createElement('iframe');
      iframe.onload = function() { iframe.parentNode.removeChild(iframe); };
      iframe.style.display = 'none';
      iframe.src = 'consent://consent/?' + queryStringFromDictionary(info);
      document.body.appendChild(iframe);
    };

    function validateRawResponse(rawResponse) {
      var adNetworks = rawResponse['ad_network_ids'] || [];
      if (!adNetworks) {
        throw Error('Error: invalid ad networks.');
      }
    }

    function setUpConsentDialog(argsDictionary) {
      argsDictionary = argsDictionary || {};
      var args = argsDictionary['args'];
      var infoJSON = args['info'] || '';

      var formInfo;
      try {
        formInfo = JSON.parse(infoJSON) || {};
      } catch(e) {
        formLoadCompleted('Error: consent SDK passed invalid data to the ' +
                          'consent form. ' + e.message);
        return;
      }

      var consentInfo = formInfo['consent_info'] || {};
      var rawResponseJSON = consentInfo['raw_response'] || '';

      if (!rawResponseJSON) {
        var method;
        if (formInfo['plat'] === 'ios') {
          method = '-[PACConsentInformation ' +
              'requestConsentInfoUpdateForPublisherIdentifiers:completionHandler:]';
        } else {
          method = 'com.google.ads.consent.ConsentInformation.requestConsentInfoUpdate()';
        }
        var message = 'Error: no information available. Successful call to ' +
            method + ' required before using this form.';
        formLoadCompleted(message);
        return;
      }

      var rawResponse;
      try {
        rawResponse = JSON.parse(rawResponseJSON) || {};
      } catch(e) {
        formLoadCompleted('Error: consent form received invalid data from ' +
                          'the consent server. ' + e.message);
        return;
      }

      try {
        validateRawResponse(rawResponse);
      } catch(e) {
        formLoadCompleted(e.message);
        return;
      }

      // iOS may encode the boolean info as numbers (0/1) or Booleans.

      // Don't show for child users.
      var isChild = consentInfo['tag_for_under_age_of_consent'] || false;
      if (isChild) {
        formLoadCompleted('Error: tagged for under age of consent.');
        return;
      }

      // Consent not required if request is not EEA or unknown.
      if (!consentInfo['is_request_in_eea_or_unknown']) {
        formLoadCompleted('Error: request is not in EEA or unknown.');
        return;
      }

      // Set app name.
      var appName = formInfo['app_name'] || '';
      if (appName.length <= 0) {
        formLoadCompleted('Error: invalid app name.');
      }
      var appNameEls = document.getElementsByClassName('app_name');
      for (var i = 0; i < appNameEls.length; i++) {
        appNameEls[i].innerText = appName;
      }

      // Add the app icon.
      var appIconSrc = formInfo['app_icon'] || '';
      if (appIconSrc) {
        var appIconEl = document.createElement('img');
        appIconEl.src = appIconSrc;
        appIconEl.id = 'app_icon';
        appIconEl.className = 'app_icon';
        obj.titleAppNameEl.parentNode.insertBefore(appIconEl, obj.titleAppNameEl);
      }

      // Returns an onclick handler that opens a URL in a browser.
      function createPolicyUrlOnClick(url) {
        return function() {
          openBrowser(url);
          return false;  // Return false to prevent default click logic.
        };
      };

      // Create the app's privacy policy URL handler.
      var app_privacy_url = formInfo['app_privacy_url'];
      if (!app_privacy_url) {
        formLoadCompleted('Error: must provided app privacy URL.');
        return;
      }
      showAppPrivacyPolicy = createPolicyUrlOnClick(app_privacy_url);

      // Update provider list.
      var providers = consentInfo['providers'] || [];
      var providersLen = providers.length;

      obj.providersEl.innerHTML = '';
      var providersList = document.createElement('ul');
      providersList.setAttribute('id', 'providersList');
      for (var i = 0; i < providersLen; i++) {
        var provider = providers[i];
        var providerTag = document.createElement('a');
        providerTag.innerText = provider['company_name'];
        providerTag.onclick = createPolicyUrlOnClick(provider['policy_url']);

        var providerListItem = document.createElement('li');
        providerListItem.appendChild(providerTag);
        providersList.appendChild(providerListItem);
      }
      obj.providersEl.appendChild(providersList);

      // Hide unavailable options.
      var hasValidProviderCount = providersLen > 0;
      if (hasValidProviderCount) {
        document.getElementById('providers_count').innerText = providersLen;
      }
      var hasAnyNonPersonalizedOnlyPubId = consentInfo['has_any_npa_pub_id'] || false;
      if (!formInfo['offer_personalized'] ||
          !hasValidProviderCount ||
          hasAnyNonPersonalizedOnlyPubId) {
        obj.btn0El.parentNode.removeChild(obj.btn0El);
      }
      if (!formInfo['offer_non_personalized']) {
        obj.btn1El.parentNode.removeChild(obj.btn1El);
      }
      if (!formInfo['offer_ad_free']) {
        obj.btn2El.parentNode.removeChild(obj.btn2El);
      }

      // Form must have at least one of the personalized or non-personalized options.
      if (!obj.btn0El.parentElement && !obj.btn1El.parentElement) {
        formLoadCompleted('Error: no options.');
        return;
      }

      var buttons = obj.buttonsEl;
      if (buttons.childElementCount == 2 && !obj.btn0El.parentNode) {
        showState('NPA-AdFree');
      } if (buttons.childElementCount == 1 && obj.btn0El.parentNode) {
        showState('PA-only');
      } if (buttons.childElementCount == 1 && obj.btn1El.parentNode) {
        showState('NPA-only');
      }

      formLoadCompleted('success');
    }

    function showState(stateName) {
      switch (stateName) {
        case 'NPA-AdFree':
          // Show NPA and AdFree buttons
          obj.contentEl.classList.add('NPA-AdFree');
          obj.headQuestionEl.parentNode.removeChild(obj.headQuestionEl);
          obj.btn1TitleEl.innerHTML = 'See ads that are less relevant';
          obj.headDetailTextEl.innerHTML = 'If you choose to see ads, we’ll partner with Google and use a unique identifier on your device.';
          break;
        case 'PA-only':
          // Show PA-only consent if it's the only available option.
          obj.headDetailChangeEl.classList.add('no-display');
          obj.consent0ChangeEl.classList.add('no-display');
          break;
        case 'NPA-only':
          // Show NPA-only consent if it's the only available option.
          obj.contentEl.classList.add('NPA-only');
          obj.optionsEl.classList.add('no-display');
          obj.backButtonEl.classList.add('no-display');
          obj.consent2El.classList.remove('no-display');
          break;
      }
    }

    document.addEventListener('DOMContentLoaded', function(event) {
      obj.overlayEl = document.getElementById('overlay');
      obj.contentEl = document.getElementById('content');
      obj.contentBoxEl = document.getElementById('content-box');
      obj.titleAppNameEl = document.getElementById('title_app_name');
      obj.optionsEl = document.getElementById('options');
      obj.titleHeadEl = document.getElementById('title_head');
      obj.headQuestionEl = document.getElementById('head_question');
      obj.headDetailChangeEl = document.getElementById('change_anytime_text1');
      obj.headDetailTextEl = document.getElementById('head_detail_text');
      obj.buttonsEl = document.getElementById('buttons');
      obj.btn0El = document.getElementById('btn0');
      obj.btn1El = document.getElementById('btn1');
      obj.btn2El = document.getElementById('btn2');
      obj.btn1TitleEl = document.getElementById('btn1_title');
      obj.consent0El = document.getElementById('consent0');
      obj.consent0ChangeEl = document.getElementById('change_anytime_text2');
      obj.consent1El = document.getElementById('consent1');
      obj.consent2El = document.getElementById('consent2');
      obj.providersEl = document.getElementById('providers');
      obj.backButtonEl = document.getElementById('back-btn');
    });

    window.onresize = function(event) {
      if (state === 'consent') {
        // this makes the height of consent modal grow/shrink automatically
        obj.contentEl.style.height = 'auto';
      }
    };

    // Make the consent modal fade-in in the beginning (after delay (consentDelayMs))
    setTimeout(function() {
      state = 'consent';
      obj.overlayEl.classList.remove('hidden');
    }, consentDelayMs);