<html><head><meta http-equiv="Content-Type" content="text/html; charset=utf-8"/><title>亚洲科技四方系统-对接文档</title><style>
        /* cspell:disable-file */
        /* webkit printing magic: print all background colors */
        html {
            -webkit-print-color-adjust: exact;
        }
        * {
            box-sizing: border-box;
            -webkit-print-color-adjust: exact;
        }

        html,
        body {
            margin: 0;
            padding: 0;
        }
        @media only screen {
            body {
                margin: 2em auto;
                max-width: 900px;
                color: rgb(55, 53, 47);
            }
        }

        body {
            line-height: 1.5;
            white-space: pre-wrap;
        }

        a,
        a.visited {
            color: inherit;
            text-decoration: underline;
        }

        .pdf-relative-link-path {
            font-size: 80%;
            color: #444;
        }

        h1,
        h2,
        h3 {
            letter-spacing: -0.01em;
            line-height: 1.2;
            font-weight: 600;
            margin-bottom: 0;
        }

        .page-title {
            font-size: 2.5rem;
            font-weight: 700;
            margin-top: 0;
            margin-bottom: 0.75em;
        }

        h1 {
            font-size: 1.875rem;
            margin-top: 1.875rem;
        }

        h2 {
            font-size: 1.5rem;
            margin-top: 1.5rem;
        }

        h3 {
            font-size: 1.25rem;
            margin-top: 1.25rem;
        }

        .source {
            border: 1px solid #ddd;
            border-radius: 3px;
            padding: 1.5em;
            word-break: break-all;
        }

        .callout {
            border-radius: 3px;
            padding: 1rem;
        }

        figure {
            margin: 1.25em 0;
            page-break-inside: avoid;
        }

        figcaption {
            opacity: 0.5;
            font-size: 85%;
            margin-top: 0.5em;
        }

        mark {
            background-color: transparent;
        }

        .indented {
            padding-left: 1.5em;
        }

        hr {
            background: transparent;
            display: block;
            width: 100%;
            height: 1px;
            visibility: visible;
            border: none;
            border-bottom: 1px solid rgba(55, 53, 47, 0.09);
        }

        img {
            max-width: 100%;
        }

        @media only print {
            img {
                max-height: 100vh;
                object-fit: contain;
            }
        }

        @page {
            margin: 1in;
        }

        .collection-content {
            font-size: 0.875rem;
        }

        .column-list {
            display: flex;
            justify-content: space-between;
        }

        .column {
            padding: 0 1em;
        }

        .column:first-child {
            padding-left: 0;
        }

        .column:last-child {
            padding-right: 0;
        }

        .table_of_contents-item {
            display: block;
            font-size: 0.875rem;
            line-height: 1.3;
            padding: 0.125rem;
        }

        .table_of_contents-indent-1 {
            margin-left: 1.5rem;
        }

        .table_of_contents-indent-2 {
            margin-left: 3rem;
        }

        .table_of_contents-indent-3 {
            margin-left: 4.5rem;
        }

        .table_of_contents-link {
            text-decoration: none;
            opacity: 0.7;
            border-bottom: 1px solid rgba(55, 53, 47, 0.18);
        }

        table,
        th,
        td {
            border: 1px solid rgba(55, 53, 47, 0.09);
            border-collapse: collapse;
        }

        table {
            border-left: none;
            border-right: none;
        }

        th,
        td {
            font-weight: normal;
            padding: 0.25em 0.5em;
            line-height: 1.5;
            min-height: 1.5em;
            text-align: left;
        }

        th {
            color: rgba(55, 53, 47, 0.6);
        }

        ol,
        ul {
            margin: 0;
            margin-block-start: 0.6em;
            margin-block-end: 0.6em;
        }

        li > ol:first-child,
        li > ul:first-child {
            margin-block-start: 0.6em;
        }

        ul > li {
            list-style: disc;
        }

        ul.to-do-list {
            padding-inline-start: 0;
        }

        ul.to-do-list > li {
            list-style: none;
        }

        .to-do-children-checked {
            text-decoration: line-through;
            opacity: 0.375;
        }

        ul.toggle > li {
            list-style: none;
        }

        ul {
            padding-inline-start: 1.7em;
        }

        ul > li {
            padding-left: 0.1em;
        }

        ol {
            padding-inline-start: 1.6em;
        }

        ol > li {
            padding-left: 0.2em;
        }

        .mono ol {
            padding-inline-start: 2em;
        }

        .mono ol > li {
            text-indent: -0.4em;
        }

        .toggle {
            padding-inline-start: 0em;
            list-style-type: none;
        }

        /* Indent toggle children */
        .toggle > li > details {
            padding-left: 1.7em;
        }

        .toggle > li > details > summary {
            margin-left: -1.1em;
        }

        .selected-value {
            display: inline-block;
            padding: 0 0.5em;
            background: rgba(206, 205, 202, 0.5);
            border-radius: 3px;
            margin-right: 0.5em;
            margin-top: 0.3em;
            margin-bottom: 0.3em;
            white-space: nowrap;
        }

        .collection-title {
            display: inline-block;
            margin-right: 1em;
        }

        .page-description {
            margin-bottom: 2em;
        }

        .simple-table {
            margin-top: 1em;
            font-size: 0.875rem;
            empty-cells: show;
        }
        .simple-table td {
            height: 29px;
            min-width: 120px;
        }

        .simple-table th {
            height: 29px;
            min-width: 120px;
        }

        .simple-table-header-color {
            background: rgb(247, 246, 243);
            color: black;
        }
        .simple-table-header {
            font-weight: 500;
        }

        time {
            opacity: 0.5;
        }

        .icon {
            display: inline-block;
            max-width: 1.2em;
            max-height: 1.2em;
            text-decoration: none;
            vertical-align: text-bottom;
            margin-right: 0.5em;
        }

        img.icon {
            border-radius: 3px;
        }

        .user-icon {
            width: 1.5em;
            height: 1.5em;
            border-radius: 100%;
            margin-right: 0.5rem;
        }

        .user-icon-inner {
            font-size: 0.8em;
        }

        .text-icon {
            border: 1px solid #000;
            text-align: center;
        }

        .page-cover-image {
            display: block;
            object-fit: cover;
            width: 100%;
            max-height: 30vh;
        }

        .page-header-icon {
            font-size: 3rem;
            margin-bottom: 1rem;
        }

        .page-header-icon-with-cover {
            margin-top: -0.72em;
            margin-left: 0.07em;
        }

        .page-header-icon img {
            border-radius: 3px;
        }

        .link-to-page {
            margin: 1em 0;
            padding: 0;
            border: none;
            font-weight: 500;
        }

        p > .user {
            opacity: 0.5;
        }

        td > .user,
        td > time {
            white-space: nowrap;
        }

        input[type="checkbox"] {
            transform: scale(1.5);
            margin-right: 0.6em;
            vertical-align: middle;
        }

        p {
            margin-top: 0.5em;
            margin-bottom: 0.5em;
        }

        .image {
            border: none;
            margin: 1.5em 0;
            padding: 0;
            border-radius: 0;
            text-align: center;
        }

        .code,
        code {
            background: rgba(135, 131, 120, 0.15);
            border-radius: 3px;
            padding: 0.2em 0.4em;
            border-radius: 3px;
            font-size: 85%;
            tab-size: 2;
        }

        code {
            color: #eb5757;
        }

        .code {
            padding: 1.5em 1em;
        }

        .code-wrap {
            white-space: pre-wrap;
            word-break: break-all;
        }

        .code > code {
            background: none;
            padding: 0;
            font-size: 100%;
            color: inherit;
        }

        blockquote {
            font-size: 1.25em;
            margin: 1em 0;
            padding-left: 1em;
            border-left: 3px solid rgb(55, 53, 47);
        }

        .bookmark {
            text-decoration: none;
            max-height: 8em;
            padding: 0;
            display: flex;
            width: 100%;
            align-items: stretch;
        }

        .bookmark-title {
            font-size: 0.85em;
            overflow: hidden;
            text-overflow: ellipsis;
            height: 1.75em;
            white-space: nowrap;
        }

        .bookmark-text {
            display: flex;
            flex-direction: column;
        }

        .bookmark-info {
            flex: 4 1 180px;
            padding: 12px 14px 14px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }

        .bookmark-image {
            width: 33%;
            flex: 1 1 180px;
            display: block;
            position: relative;
            object-fit: cover;
            border-radius: 1px;
        }

        .bookmark-description {
            color: rgba(55, 53, 47, 0.6);
            font-size: 0.75em;
            overflow: hidden;
            max-height: 4.5em;
            word-break: break-word;
        }

        .bookmark-href {
            font-size: 0.75em;
            margin-top: 0.25em;
        }

        .sans { font-family: ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol"; }
        .code { font-family: "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace; }
        .serif { font-family: Lyon-Text, Georgia, ui-serif, serif; }
        .mono { font-family: iawriter-mono, Nitti, Menlo, Courier, monospace; }
        .pdf .sans { font-family: Inter, ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol", 'Twemoji', 'Noto Color Emoji', 'Noto Sans CJK JP'; }
        .pdf:lang(zh-CN) .sans { font-family: Inter, ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol", 'Twemoji', 'Noto Color Emoji', 'Noto Sans CJK SC'; }
        .pdf:lang(zh-TW) .sans { font-family: Inter, ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol", 'Twemoji', 'Noto Color Emoji', 'Noto Sans CJK TC'; }
        .pdf:lang(ko-KR) .sans { font-family: Inter, ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol", 'Twemoji', 'Noto Color Emoji', 'Noto Sans CJK KR'; }
        .pdf .code { font-family: Source Code Pro, "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace, 'Twemoji', 'Noto Color Emoji', 'Noto Sans Mono CJK JP'; }
        .pdf:lang(zh-CN) .code { font-family: Source Code Pro, "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace, 'Twemoji', 'Noto Color Emoji', 'Noto Sans Mono CJK SC'; }
        .pdf:lang(zh-TW) .code { font-family: Source Code Pro, "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace, 'Twemoji', 'Noto Color Emoji', 'Noto Sans Mono CJK TC'; }
        .pdf:lang(ko-KR) .code { font-family: Source Code Pro, "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace, 'Twemoji', 'Noto Color Emoji', 'Noto Sans Mono CJK KR'; }
        .pdf .serif { font-family: PT Serif, Lyon-Text, Georgia, ui-serif, serif, 'Twemoji', 'Noto Color Emoji', 'Noto Serif CJK JP'; }
        .pdf:lang(zh-CN) .serif { font-family: PT Serif, Lyon-Text, Georgia, ui-serif, serif, 'Twemoji', 'Noto Color Emoji', 'Noto Serif CJK SC'; }
        .pdf:lang(zh-TW) .serif { font-family: PT Serif, Lyon-Text, Georgia, ui-serif, serif, 'Twemoji', 'Noto Color Emoji', 'Noto Serif CJK TC'; }
        .pdf:lang(ko-KR) .serif { font-family: PT Serif, Lyon-Text, Georgia, ui-serif, serif, 'Twemoji', 'Noto Color Emoji', 'Noto Serif CJK KR'; }
        .pdf .mono { font-family: PT Mono, iawriter-mono, Nitti, Menlo, Courier, monospace, 'Twemoji', 'Noto Color Emoji', 'Noto Sans Mono CJK JP'; }
        .pdf:lang(zh-CN) .mono { font-family: PT Mono, iawriter-mono, Nitti, Menlo, Courier, monospace, 'Twemoji', 'Noto Color Emoji', 'Noto Sans Mono CJK SC'; }
        .pdf:lang(zh-TW) .mono { font-family: PT Mono, iawriter-mono, Nitti, Menlo, Courier, monospace, 'Twemoji', 'Noto Color Emoji', 'Noto Sans Mono CJK TC'; }
        .pdf:lang(ko-KR) .mono { font-family: PT Mono, iawriter-mono, Nitti, Menlo, Courier, monospace, 'Twemoji', 'Noto Color Emoji', 'Noto Sans Mono CJK KR'; }
        .highlight-default {
            color: rgba(55, 53, 47, 1);
        }
        .highlight-gray {
            color: rgba(120, 119, 116, 1);
            fill: rgba(120, 119, 116, 1);
        }
        .highlight-brown {
            color: rgba(159, 107, 83, 1);
            fill: rgba(159, 107, 83, 1);
        }
        .highlight-orange {
            color: rgba(217, 115, 13, 1);
            fill: rgba(217, 115, 13, 1);
        }
        .highlight-yellow {
            color: rgba(203, 145, 47, 1);
            fill: rgba(203, 145, 47, 1);
        }
        .highlight-teal {
            color: rgba(68, 131, 97, 1);
            fill: rgba(68, 131, 97, 1);
        }
        .highlight-blue {
            color: rgba(51, 126, 169, 1);
            fill: rgba(51, 126, 169, 1);
        }
        .highlight-purple {
            color: rgba(144, 101, 176, 1);
            fill: rgba(144, 101, 176, 1);
        }
        .highlight-pink {
            color: rgba(193, 76, 138, 1);
            fill: rgba(193, 76, 138, 1);
        }
        .highlight-red {
            color: rgba(212, 76, 71, 1);
            fill: rgba(212, 76, 71, 1);
        }
        .highlight-gray_background {
            background: rgba(241, 241, 239, 1);
        }
        .highlight-brown_background {
            background: rgba(244, 238, 238, 1);
        }
        .highlight-orange_background {
            background: rgba(251, 236, 221, 1);
        }
        .highlight-yellow_background {
            background: rgba(251, 243, 219, 1);
        }
        .highlight-teal_background {
            background: rgba(237, 243, 236, 1);
        }
        .highlight-blue_background {
            background: rgba(231, 243, 248, 1);
        }
        .highlight-purple_background {
            background: rgba(244, 240, 247, 0.8);
        }
        .highlight-pink_background {
            background: rgba(249, 238, 243, 0.8);
        }
        .highlight-red_background {
            background: rgba(253, 235, 236, 1);
        }
        .block-color-default {
            color: inherit;
            fill: inherit;
        }
        .block-color-gray {
            color: rgba(120, 119, 116, 1);
            fill: rgba(120, 119, 116, 1);
        }
        .block-color-brown {
            color: rgba(159, 107, 83, 1);
            fill: rgba(159, 107, 83, 1);
        }
        .block-color-orange {
            color: rgba(217, 115, 13, 1);
            fill: rgba(217, 115, 13, 1);
        }
        .block-color-yellow {
            color: rgba(203, 145, 47, 1);
            fill: rgba(203, 145, 47, 1);
        }
        .block-color-teal {
            color: rgba(68, 131, 97, 1);
            fill: rgba(68, 131, 97, 1);
        }
        .block-color-blue {
            color: rgba(51, 126, 169, 1);
            fill: rgba(51, 126, 169, 1);
        }
        .block-color-purple {
            color: rgba(144, 101, 176, 1);
            fill: rgba(144, 101, 176, 1);
        }
        .block-color-pink {
            color: rgba(193, 76, 138, 1);
            fill: rgba(193, 76, 138, 1);
        }
        .block-color-red {
            color: rgba(212, 76, 71, 1);
            fill: rgba(212, 76, 71, 1);
        }
        .block-color-gray_background {
            background: rgba(241, 241, 239, 1);
        }
        .block-color-brown_background {
            background: rgba(244, 238, 238, 1);
        }
        .block-color-orange_background {
            background: rgba(251, 236, 221, 1);
        }
        .block-color-yellow_background {
            background: rgba(251, 243, 219, 1);
        }
        .block-color-teal_background {
            background: rgba(237, 243, 236, 1);
        }
        .block-color-blue_background {
            background: rgba(231, 243, 248, 1);
        }
        .block-color-purple_background {
            background: rgba(244, 240, 247, 0.8);
        }
        .block-color-pink_background {
            background: rgba(249, 238, 243, 0.8);
        }
        .block-color-red_background {
            background: rgba(253, 235, 236, 1);
        }
        .select-value-color-uiBlue { background-color: rgba(35, 131, 226, .07); }
        .select-value-color-pink { background-color: rgba(245, 224, 233, 1); }
        .select-value-color-purple { background-color: rgba(232, 222, 238, 1); }
        .select-value-color-green { background-color: rgba(219, 237, 219, 1); }
        .select-value-color-gray { background-color: rgba(227, 226, 224, 1); }
        .select-value-color-translucentGray { background-color: rgba(255, 255, 255, 0.0375); }
        .select-value-color-orange { background-color: rgba(250, 222, 201, 1); }
        .select-value-color-brown { background-color: rgba(238, 224, 218, 1); }
        .select-value-color-red { background-color: rgba(255, 226, 221, 1); }
        .select-value-color-yellow { background-color: rgba(253, 236, 200, 1); }
        .select-value-color-blue { background-color: rgba(211, 229, 239, 1); }
        .select-value-color-pageGlass { background-color: undefined; }
        .select-value-color-washGlass { background-color: undefined; }

        .checkbox {
            display: inline-flex;
            vertical-align: text-bottom;
            width: 16;
            height: 16;
            background-size: 16px;
            margin-left: 2px;
            margin-right: 5px;
        }

        .checkbox-on {
            background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%2216%22%20height%3D%2216%22%20viewBox%3D%220%200%2016%2016%22%20fill%3D%22none%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%0A%3Crect%20width%3D%2216%22%20height%3D%2216%22%20fill%3D%22%2358A9D7%22%2F%3E%0A%3Cpath%20d%3D%22M6.71429%2012.2852L14%204.9995L12.7143%203.71436L6.71429%209.71378L3.28571%206.2831L2%207.57092L6.71429%2012.2852Z%22%20fill%3D%22white%22%2F%3E%0A%3C%2Fsvg%3E");
        }

        .checkbox-off {
            background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%2216%22%20height%3D%2216%22%20viewBox%3D%220%200%2016%2016%22%20fill%3D%22none%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%0A%3Crect%20x%3D%220.75%22%20y%3D%220.75%22%20width%3D%2214.5%22%20height%3D%2214.5%22%20fill%3D%22white%22%20stroke%3D%22%2336352F%22%20stroke-width%3D%221.5%22%2F%3E%0A%3C%2Fsvg%3E");
        }

    </style></head><body><article id="4333300c-eb71-42ad-a509-835d54bb6523" class="page sans"><header><h1 class="page-title">亚洲科技四方系统-对接文档</h1><p class="page-description"></p></header><div class="page-body"><p id="a2a7689c-7b67-4f4b-a64d-a530db8bbd23" class="">🔸<strong>优点</strong>：<br/>     独立海外服务器，极速响应，后台丝滑不卡顿，实测每分钟15000+订单依然流畅，UI简洁美观，精细化的权限管理，严谨的细节设计防止误操作，技术客服24小时在线<br/></p><p id="2d33dc7c-cd02-48c7-b87c-4c4fcef71674" class="">     基于springboot+mysql开发，集成activeMQ、redis、zookeeper等最大限度保证高性能、抗高并发技术团队全新开发，从根本上杜绝了市面上流传源码的漏洞，功能俱全、服务优质、行业顶流</p><p id="f5e596cd-8a81-4042-ae25-8cc0c293ae64" class="">🔸<strong>独家特色功能</strong>：<br/>     严保资金安全！！通道配置修改警报，手动补单数过多警报，订单异常警报，一键日切，一键复制通道，通道定时开关等等..及全功能机器人🤖免费送。<br/></p><p id="a938f34a-f797-4254-ae66-79fb58a92db5" class="">唯一联系人：❤️<a href="https://t.me/d999999">@D999999</a>❤️</p><p id="49988f7a-45fe-48d1-af36-b657000fb94a" class="">四方行业交流群:  ❤️<a href="https://t.me/vipdou">@vipdou</a>❤️</p><h2 id="8cc5682c-0255-4f86-958e-617d3883287d" class=""><strong>参数规范</strong></h2><p id="e31f4c1d-bbd3-4f80-ace7-ed366e3848d8" class="">交易金额：默认为人民币交易，单位为分，参数值不能带小数。</p><p id="1bdaee71-825f-4e2e-9b0c-03de73a5d93f" class="">时间参数：所有涉及时间参数均使用精确到<strong>毫秒</strong>的13位数值，如：1622016572190。时间戳具体是指从格林尼治时间1970年01月01日00时00分00秒起至现在的毫秒数。</p><h2 id="ac6e770b-4dd1-4364-bb2f-536482adc815" class=""><strong>签名算法</strong></h2><p id="a0087c32-91ec-4988-a409-393e92057e2c" class=""><code>签名生成的通用步骤如下</code></p><p id="b4d5db11-815a-4f61-9adc-006e9f5d7c7f" class=""><em><strong>第一步：</strong></em> 设所有发送或者接收到的数据为集合M，将集合M内非空参数值的参数按照参数名ASCII码从小到大排序（字典序），使用URL键值对的格式（即key1=value1&amp;key2=value2…）拼接成字符串stringA。</p><p id="c88460a0-9535-42d0-9489-9ddd546f8781" class="">特别注意以下重要规则：</p><p id="2ec03b7c-4ea8-43d4-98d6-bca20d17b30c" class="">◆ 参数名ASCII码从小到大排序（字典序）；</p><p id="28a04541-2ea7-4fee-af51-7c128417965f" class="">◆ 如果参数的值为空不参与签名；</p><p id="006ac31b-9e48-4f9a-9079-7a1c3a5c3656" class="">◆ 参数名区分大小写；</p><p id="41661de3-f4f6-40a5-9619-52837be8ff95" class="">◆ 验证调用返回或支付中心主动通知签名时，传送的sign参数不参与签名，将生成的签名与该sign值作校验。</p><p id="16286bd6-849d-48c7-bb80-1d2f6e86b2ed" class="">◆ 支付中心接口可能增加字段，验证签名时必须支持增加的扩展字段</p><p id="ca9b5dfe-e045-4b78-8e8b-b47d81245c67" class=""><em><strong>第二步：</strong></em> 在stringA最后拼接上key<code>[即 StringA +&quot;&amp;key=&quot; + 私钥 ]</code> 得到stringSignTemp字符串，并对stringSignTemp进行MD5运算，再将得到的字符串所有字符转换为大写，得到sign值signValue。</p><p id="36797333-fdd1-4c5d-a360-8d6f6a5817e8" class="">如请求支付系统参数如下：</p><script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js" integrity="sha512-7Z9J3l1+EYfeaPKcGXu3MS/7T+w19WtKQY/n+xzmw4hZhJ9tyYmcUS+4QqAlzhicE5LAfMQSF3iFTK9bQdTxXg==" crossorigin="anonymous" referrerPolicy="no-referrer"></script><link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism.min.css" integrity="sha512-tN7Ec6zAFaVSG3TpNAKtk4DOHNpSwKHxxrsiw4GHKESGPs5njn/0sMCUMl2svV4wo4BK/rCP7juYz+zx+l6oeQ==" crossorigin="anonymous" referrerPolicy="no-referrer"/><pre id="03e78f4c-7e98-42b3-b282-0db22bdf1aab" class="code"><code class="language-JavaScript" style="white-space:pre-wrap;word-break:break-all">Map signMap =new HashMap&lt;&gt;();
signMap.put(&quot;mchNo&quot;, &quot;M1693765800&quot;);
signMap.put(&quot;mchOrderNo&quot;, &quot;AT12023090521025312632&quot;);
signMap.put(&quot;amount&quot;, &quot;10000&quot;);
signMap.put(&quot;clientIp&quot;, &quot;192.168.0.111&quot;);
signMap.put(&quot;productId&quot;, &quot;1000&quot;);
signMap.put(&quot;notifyUrl&quot;, &quot;https://www.baidu.com&quot;);
signMap.put(&quot;reqTime&quot;, &quot;1693918973130&quot;);</code></pre><p id="efdc2dda-0db4-43e7-8470-178b16df89f1" class=""><code>待签名值</code>：</p><p id="b9455ffa-91bd-4adf-9909-03766d500a98" class="">amount=5000&amp;clientIp=127.0.0.1&amp;mchNo=M1693765800&amp;mchOrderNo=AT12023090521025312632&amp;notifyUrl=https://www.baidu.com&amp;productId=8089&amp;reqTime=1693918973130&amp;key=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX</p><p id="70d07a48-f158-4bd5-b154-1fa271b6d17e" class=""><code>签名结果</code>：EF64BADC33E9877E30E1E8B9C603E2F4</p><p id="fe7af860-84bc-4946-b553-b952ec889975" class=""><code>最终请求支付系统参数</code>：amount=5000&amp;clientIp=127.0.0.1&amp;mchNo=M1693765800&amp;mchOrderNo=AT12023090521025312632&amp;notifyUrl=https://www.baidu.com&amp;productId=8089&amp;reqTime=1693918973130<a href="https://www.baidu.com%26platid%3D1000%26reqtime%3D20190723141000%26returnurl%3Dhttps//www.baidu.com&amp;version=1.0&amp;sign=4A5078DABBCE0D9C4E7668DACB96FF7A">&amp;sign=4A5078DABBCE0D9C4E7668DACB96FF7A</a></p><h2 id="cd63922e-654a-489a-bb68-9f3b6af52a02" class=""><strong>统一下单</strong></h2><blockquote id="4b90e592-12b6-44b3-a251-24d2e25e3d74" class="">接口说明</blockquote><p id="f3557d90-d554-4edf-8cb9-3c82aa01f14e" class="">请求URL：<a href="https://pay.jeepay.vip/api/pay/unifiedOrder">支付网关地址/api/pay/unifiedOrder</a></p><p id="76e538ec-3664-4f9b-aebf-cb7626bfa045" class="">请求方式：<code>POST</code></p><p id="14a024c9-0d83-4ba2-aba1-24930a68d756" class="">请求类型：<code>application/json</code> 或 <code>application/x-www-form-urlencoded</code></p><blockquote id="057c885b-3081-4a61-a829-7cc8382871b6" class="">请求参数</blockquote><table id="22ed638e-dd3d-4b89-bf50-7b8dab108912" class="simple-table"><tbody><tr id="85fb1854-1135-41ec-b621-5763b555f90b"><td id="lKe=" class="">字段名</td><td id="Ys@a" class="">变量名</td><td id="V^&lt;y" class="">必填</td><td id="`&lt;}m" class="">类型</td><td id=":QlN" class="">示例值</td><td id="S&lt;t[" class="">描述</td></tr><tr id="f64c27ff-860e-4319-902e-d4f6aae560e1"><td id="lKe=" class="">商户号</td><td id="Ys@a" class="">mchNo</td><td id="V^&lt;y" class="">是</td><td id="`&lt;}m" class="">String(30)</td><td id=":QlN" class="">M1621873433953</td><td id="S&lt;t[" class="">商户号</td></tr><tr id="889eab1a-4045-4915-9f19-b5cbd9378e51"><td id="lKe=" class="">商户订单号</td><td id="Ys@a" class="">mchOrderNo</td><td id="V^&lt;y" class="">是</td><td id="`&lt;}m" class="">String(30)</td><td id=":QlN" class="">20160427210604000490</td><td id="S&lt;t[" class="">商户生成的订单号</td></tr><tr id="496ac5cb-7445-4690-9d39-dd2e143ac8ce"><td id="lKe=" class="">支付产品ID</td><td id="Ys@a" class="">productId</td><td id="V^&lt;y" class="">是</td><td id="`&lt;}m" class="">String(30)</td><td id=":QlN" class="">1000</td><td id="S&lt;t[" class="">支付产品ID，具体请与平台管理联系</td></tr><tr id="1c0b2a03-e331-4d0f-b3ed-f1af6d562a7a"><td id="lKe=" class="">支付金额</td><td id="Ys@a" class="">amount</td><td id="V^&lt;y" class="">是</td><td id="`&lt;}m" class="">int</td><td id=":QlN" class="">100</td><td id="S&lt;t[" class="">支付金额,单位<strong>分</strong></td></tr><tr id="c7cf1d04-6901-454c-9432-fb42ded7a1bb"><td id="lKe=" class="">客户端IP</td><td id="Ys@a" class="">clientIp</td><td id="V^&lt;y" class="">是</td><td id="`&lt;}m" class="">String(32)</td><td id=":QlN" class="">210.73.10.148</td><td id="S&lt;t[" class="">尽量传客户端IPV4地址，某些通道不支持ipv6</td></tr><tr id="37ba2a49-9b2d-4f3c-914b-6775a4bb99c1"><td id="lKe=" class="">异步通知地址</td><td id="Ys@a" class="">notifyUrl</td><td id="V^&lt;y" class="">是</td><td id="`&lt;}m" class="">String(128)</td><td id=":QlN" class=""><a href="https://www.jeequan.com/notify.htm">https://w</a>ww.google.com</td><td id="S&lt;t[" class="">支付结果异步回调URL,只有传了该值才会发起回调</td></tr><tr id="9b40e10d-9649-4c63-82ad-cc1a70d22e67"><td id="lKe=" class="">请求时间</td><td id="Ys@a" class="">reqTime</td><td id="V^&lt;y" class="">是</td><td id="`&lt;}m" class="">long</td><td id=":QlN" class=""></td><td id="S&lt;t[" class="">请求接口时间,<strong>13位时间戳，毫秒</strong></td></tr><tr id="b4220c9e-5d56-44cf-9d4f-f5c624583e52"><td id="lKe=" class="">额外参数</td><td id="Ys@a" class="">extParam</td><td id="V^&lt;y" class="">否</td><td id="`&lt;}m" class="">String(512)</td><td id=":QlN" class="">userName</td><td id="S&lt;t[" class="">额外参数，某些特定通道需要用到</td></tr><tr id="846de779-79f9-40a7-8564-ef6f2bd6ef37"><td id="lKe=" class="">签名</td><td id="Ys@a" class="">sign</td><td id="V^&lt;y" class="">是</td><td id="`&lt;}m" class="">String(32)</td><td id=":QlN" class="">C380BEC2BFD727A4B6845133519F3AD6</td><td id="S&lt;t[" class="">签名值，详见签名算法</td></tr></tbody></table><p id="b7af45c4-e087-4ab3-860a-27eb5a6afc77" class=""><code>请求示例数据</code></p><script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js" integrity="sha512-7Z9J3l1+EYfeaPKcGXu3MS/7T+w19WtKQY/n+xzmw4hZhJ9tyYmcUS+4QqAlzhicE5LAfMQSF3iFTK9bQdTxXg==" crossorigin="anonymous" referrerPolicy="no-referrer"></script><link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism.min.css" integrity="sha512-tN7Ec6zAFaVSG3TpNAKtk4DOHNpSwKHxxrsiw4GHKESGPs5njn/0sMCUMl2svV4wo4BK/rCP7juYz+zx+l6oeQ==" crossorigin="anonymous" referrerPolicy="no-referrer"/><pre id="54fdd7f7-d4da-4f13-b3af-af18e5356bfe" class="code"><code class="language-JSON" style="white-space:pre-wrap;word-break:break-all">{
  &quot;amount&quot;: 80,
  &quot;mchOrderNo&quot;: &quot;mho1624005107281&quot;,
  &quot;productId&quot;: &quot;1000&quot;,
  &quot;reqTime&quot;: &quot;1624005107&quot;,
  &quot;clientIp&quot;: &quot;192.166.1.132&quot;,
  &quot;notifyUrl&quot;: &quot;https://www.google.com&quot;,
  &quot;mchNo&quot;: &quot;M1623984572&quot;,
  &quot;sign&quot;: &quot;84F606FA25A6EC4783BECC08D4FDC681&quot;
}</code></pre><blockquote id="a50b92dd-db87-4186-8c8b-bc082d98a2e7" class="">返回参数</blockquote><table id="ee35b29f-a0d6-412e-b32c-4efd7940e327" class="simple-table"><tbody><tr id="8660fc9c-11c2-4ce5-9a57-fac07b50145a"><td id="`UTJ" class="">字段名</td><td id="uSB&gt;" class="">变量名</td><td id="suq]" class="">必填</td><td id="s_yL" class="">类型</td><td id="oIEH" class="">示例值</td><td id=":X=z" class="">描述</td></tr><tr id="fbe9c429-11c1-4312-9d3b-4e30ad0ac40c"><td id="`UTJ" class="">返回状态</td><td id="uSB&gt;" class="">code</td><td id="suq]" class="">是</td><td id="s_yL" class="">int</td><td id="oIEH" class="">0</td><td id=":X=z" class="">0-处理成功，其他-处理有误，详见错误码</td></tr><tr id="dafdb923-0305-4f90-919f-0296b267548d"><td id="`UTJ" class="">返回信息</td><td id="uSB&gt;" class="">msg</td><td id="suq]" class="">否</td><td id="s_yL" class="">String(128)</td><td id="oIEH" class="">签名失败</td><td id=":X=z" class="">具体错误原因，例如：签名失败、参数格式校验错误</td></tr><tr id="548ef775-2a9e-4fd3-b399-f5ce31665519"><td id="`UTJ" class="">签名信息</td><td id="uSB&gt;" class="">sign</td><td id="suq]" class="">否</td><td id="s_yL" class="">String(32)</td><td id="oIEH" class="">CCD9083A6DAD9A2DA9F668C3D4517A84</td><td id=":X=z" class="">对data内数据签名,如data为空则不返回</td></tr><tr id="6d700a42-944e-4551-9867-3f16574b92b1"><td id="`UTJ" class="">返回数据</td><td id="uSB&gt;" class="">data</td><td id="suq]" class="">否</td><td id="s_yL" class="">String(512)</td><td id="oIEH" class="">{}</td><td id=":X=z" class="">返回下单数据,json格式数据</td></tr></tbody></table><p id="26594c0e-7c2d-4232-beab-66bd0fd278dd" class=""><code>data数据格式</code></p><table id="05a84327-6131-42b0-b933-31a11c947832" class="simple-table"><tbody><tr id="8263e130-66c2-4752-ad6a-4c2c7f65e5e9"><td id="\nNF" class="">字段名</td><td id="em;I" class="">变量名</td><td id="vBOh" class="">必填</td><td id="ST^O" class="">类型</td><td id="tT{T" class="">示例值</td><td id=":wzO" class="">描述</td></tr><tr id="568d3cd2-7b0e-4cff-a43a-c7e1edc406c9"><td id="\nNF" class="">支付订单号</td><td id="em;I" class="">payOrderId</td><td id="vBOh" class="">是</td><td id="ST^O" class="">String(30)</td><td id="tT{T" class="">U12021022311124442600</td><td id=":wzO" class="">返回支付系统订单号</td></tr><tr id="7d5fc28a-dc27-4b48-87d0-f2b0f6cc512b"><td id="\nNF" class="">商户订单号</td><td id="em;I" class="">mchOrderNo</td><td id="vBOh" class="">是</td><td id="ST^O" class="">String(30)</td><td id="tT{T" class="">20160427210604000490</td><td id=":wzO" class="">返回商户传入的订单号</td></tr><tr id="55a835c2-bb79-4729-ab76-a7090dc1156d"><td id="\nNF" class="">订单状态</td><td id="em;I" class="">orderState</td><td id="vBOh" class="">是</td><td id="ST^O" class="">int</td><td id="tT{T" class="">1</td><td id=":wzO" class="">支付订单状态<br/>1-支付中<br/>3-支付失败<br/>7-出码失败<br/></td></tr><tr id="f4415ac7-751a-47a7-aff3-4b6f90576395"><td id="\nNF" class="">支付数据类型</td><td id="em;I" class="">payDataType</td><td id="vBOh" class="">否</td><td id="ST^O" class="">String</td><td id="tT{T" class="">payUrl</td><td id=":wzO" class="">支付参数类型payUrl，此处是固定值</td></tr><tr id="ee5efbd3-0bc5-4768-9667-cb6e93e66dde"><td id="\nNF" class="">支付数据</td><td id="em;I" class="">payData</td><td id="vBOh" class="">否</td><td id="ST^O" class="">String</td><td id="tT{T" class=""><a href="http://www.jeequan.com/pay.html">http://www.</a>google.com</td><td id=":wzO" class="">支付页链接（当orderState状态为1时有值）</td></tr></tbody></table><p id="273129a1-0891-4257-9d41-990c568d7276" class=""><code>返回示例数据</code></p><script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js" integrity="sha512-7Z9J3l1+EYfeaPKcGXu3MS/7T+w19WtKQY/n+xzmw4hZhJ9tyYmcUS+4QqAlzhicE5LAfMQSF3iFTK9bQdTxXg==" crossorigin="anonymous" referrerPolicy="no-referrer"></script><link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism.min.css" integrity="sha512-tN7Ec6zAFaVSG3TpNAKtk4DOHNpSwKHxxrsiw4GHKESGPs5njn/0sMCUMl2svV4wo4BK/rCP7juYz+zx+l6oeQ==" crossorigin="anonymous" referrerPolicy="no-referrer"/><pre id="01f07e71-9d26-42ab-a523-5e4a9250de53" class="code"><code class="language-JSON" style="white-space:pre-wrap;word-break:break-all">{
  &quot;code&quot;: 0,
  &quot;data&quot;: {
    &quot;payDataType&quot;: &quot;payUrl&quot;,
    &quot;payData&quot;: &quot;http://www.google.com/testpay&quot;,
    &quot;mchOrderNo&quot;: &quot;mho1624005752661&quot;,
    &quot;orderState&quot;: 1,
    &quot;payOrderId&quot;: &quot;P202106181642329900002&quot;
  },
  &quot;msg&quot;: &quot;SUCCESS&quot;,
  &quot;sign&quot;: &quot;F4DA202C516D1F33A12F1E547C5004FD&quot;
}</code></pre><h2 id="8451a1d6-9535-4b3a-9b75-ca416fa8c2da" class=""><strong>查询订单</strong></h2><p id="df6a4975-7d5b-4649-8b89-878385df6e22" class="">商户通过该接口查询订单，支付网关会返回订单最新的数据</p><blockquote id="78f56164-7a04-4753-bc79-af52c8978a39" class="">接口说明</blockquote><p id="1d893e07-37af-4892-8c63-188a2856b053" class="">请求URL：<a href="https://pay.jeepay.vip/api/pay/unifiedOrder">支付网关地址</a><a href="https://pay.jeepay.vip/api/pay/query">/api/pay/query</a></p><p id="c2cfe877-0c03-49c2-a9dd-8ab474914d26" class="">请求方式：<code>POST</code></p><p id="09dc4306-f892-4cba-8214-09583ab9f128" class="">请求类型：<code>application/json</code> 或 <code>application/x-www-form-urlencoded</code></p><blockquote id="7b7d7149-1a98-4b34-a90d-33ef3976d302" class="">请求参数</blockquote><table id="83633ff4-4940-4042-b748-18aea472ab1a" class="simple-table"><tbody><tr id="a5104ed3-0a26-4d03-ae10-f6fb2b4a4712"><td id="{i@R" class="">字段名</td><td id="_zMr" class="">变量名</td><td id="&lt;~nN" class="">必填</td><td id="otMY" class="">类型</td><td id="ijRH" class="">示例值</td><td id="wA&lt;v" class="">描述</td></tr><tr id="b5f9dcf5-eb45-4189-a8a8-e76260b43ddd"><td id="{i@R" class="">商户号</td><td id="_zMr" class="">mchNo</td><td id="&lt;~nN" class="">是</td><td id="otMY" class="">String(30)</td><td id="ijRH" class="">M1621873433953</td><td id="wA&lt;v" class="">商户号</td></tr><tr id="edaf7e78-95d8-4e04-9e14-861b92b4aa3c"><td id="{i@R" class="">订单金额</td><td id="_zMr" class="">amount</td><td id="&lt;~nN" class="">是</td><td id="otMY" class="">int</td><td id="ijRH" class="">1000</td><td id="wA&lt;v" class="">订单金额，单位<strong>分</strong></td></tr><tr id="c120b8ce-63cf-4869-be2d-93a98105212b"><td id="{i@R" class="">支付订单号</td><td id="_zMr" class="">payOrderId</td><td id="&lt;~nN" class="">是</td><td id="otMY" class="">String(30)</td><td id="ijRH" class="">P20160427210604000490</td><td id="wA&lt;v" class="">支付中心生成的订单号，与mchOrderNo二者传一即可</td></tr><tr id="6e6eaae0-0591-46e8-89cd-2b1df2d13016"><td id="{i@R" class="">商户订单号</td><td id="_zMr" class="">mchOrderNo</td><td id="&lt;~nN" class="">是</td><td id="otMY" class="">String(30)</td><td id="ijRH" class="">20160427210604000490</td><td id="wA&lt;v" class="">商户生成的订单号，与payOrderId二者传一即可</td></tr><tr id="5d0a0470-38fc-47ea-9c0d-b5fe4f522282"><td id="{i@R" class="">请求时间</td><td id="_zMr" class="">reqTime</td><td id="&lt;~nN" class="">是</td><td id="otMY" class="">long</td><td id="ijRH" class="">1622016572190</td><td id="wA&lt;v" class="">请求接口时间,13位时间戳</td></tr><tr id="9de01e51-e884-47dc-b5cf-be1b73d72428"><td id="{i@R" class="">产品ID</td><td id="_zMr" class="">productId</td><td id="&lt;~nN" class="">否</td><td id="otMY" class="">int</td><td id="ijRH" class="">1001</td><td id="wA&lt;v" class="">产品类型ID</td></tr><tr id="2c4d1e93-b1c8-4bab-a491-bcf8973f3e64"><td id="{i@R" class="">签名</td><td id="_zMr" class="">sign</td><td id="&lt;~nN" class="">是</td><td id="otMY" class="">String(32)</td><td id="ijRH" class="">C380BEC2BFD727A4B6845133519F3AD6</td><td id="wA&lt;v" class="">签名值，详见签名算法</td></tr></tbody></table><p id="2d48564d-35a8-4186-8d50-4746ab34c327" class=""><code>请求示例数据</code></p><script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js" integrity="sha512-7Z9J3l1+EYfeaPKcGXu3MS/7T+w19WtKQY/n+xzmw4hZhJ9tyYmcUS+4QqAlzhicE5LAfMQSF3iFTK9bQdTxXg==" crossorigin="anonymous" referrerPolicy="no-referrer"></script><link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism.min.css" integrity="sha512-tN7Ec6zAFaVSG3TpNAKtk4DOHNpSwKHxxrsiw4GHKESGPs5njn/0sMCUMl2svV4wo4BK/rCP7juYz+zx+l6oeQ==" crossorigin="anonymous" referrerPolicy="no-referrer"/><pre id="005685d0-2e10-4ec2-90a6-2c325520f5e1" class="code"><code class="language-JSON" style="white-space:pre-wrap;word-break:break-all">{
  &quot;payOrderId&quot;: &quot;P202106181104177050002&quot;,
  &quot;amount&quot;: 1000,
  &quot;reqTime&quot;: &quot;1624006009&quot;,
  &quot;mchNo&quot;: &quot;M1623984572&quot;,
  &quot;sign&quot;: &quot;46940C58B2F3AE426B77A297ABF4D31E&quot;,
}</code></pre><table id="92d140df-6673-43a9-96df-e7656c90a5f0" class="simple-table"><tbody><tr id="8ecc98bf-6620-40e3-81df-f6f87f109704"><td id="ux`b" class="">字段名</td><td id="cwCL" class="">变量名</td><td id="ub_Y" class="">必填</td><td id="ni@~" class="">类型</td><td id="fkDY" class="">示例值</td><td id="ougZ" class="">描述</td></tr><tr id="b08b36b3-5cdc-448f-a7e3-ef4629d7d094"><td id="ux`b" class="">返回状态</td><td id="cwCL" class="">code</td><td id="ub_Y" class="">是</td><td id="ni@~" class="">int</td><td id="fkDY" class="">0</td><td id="ougZ" class="">0-处理成功，其他-处理有误，详见错误码</td></tr><tr id="4de6969e-58f9-4431-a52c-98691eb21e88"><td id="ux`b" class="">返回信息</td><td id="cwCL" class="">msg</td><td id="ub_Y" class="">否</td><td id="ni@~" class="">String(128)</td><td id="fkDY" class="">签名失败</td><td id="ougZ" class="">具体错误原因，例如：签名失败、参数格式校验错误</td></tr><tr id="5f25b669-7646-4a84-929f-76ce8881a187"><td id="ux`b" class="">签名信息</td><td id="cwCL" class="">sign</td><td id="ub_Y" class="">否</td><td id="ni@~" class="">String(32)</td><td id="fkDY" class="">CCD9083A6DAD9A2DA9F668C3D4517A84</td><td id="ougZ" class="">对data内数据签名,如data为空则不返回</td></tr><tr id="468b5903-508d-48ce-9593-870b602681c2"><td id="ux`b" class="">返回数据</td><td id="cwCL" class="">data</td><td id="ub_Y" class="">否</td><td id="ni@~" class="">String(512)</td><td id="fkDY" class="">{}</td><td id="ougZ" class="">返回下单数据,json格式数据</td></tr></tbody></table><p id="eb851567-d65c-4e11-bdbf-b522ba68ce4a" class=""><code>data数据格式</code></p><table id="86be43cc-0e12-494b-8d4e-561400af80c3" class="simple-table"><tbody><tr id="ea552fd7-e7a0-4286-a3ba-c5dd1a4c853a"><td id="ubR}" class="">字段名</td><td id="aesD" class="">变量名</td><td id="abiu" class="">必填</td><td id="LpS]" class="">类型</td><td id="BL&gt;:" class="">示例值</td><td id="z{_\" class="">描述</td></tr><tr id="52a24468-683f-4a75-84ae-8cbc90d92e12"><td id="ubR}" class="">支付金额</td><td id="aesD" class="">amount</td><td id="abiu" class="">是</td><td id="LpS]" class="">int</td><td id="BL&gt;:" class="">100</td><td id="z{_\" class="">支付金额,单位分</td></tr><tr id="90db604a-15c9-4834-be26-6789ce9b045f"><td id="ubR}" class="">支付订单号</td><td id="aesD" class="">payOrderId</td><td id="abiu" class="">是</td><td id="LpS]" class="">String(30)</td><td id="BL&gt;:" class="">P12021022311124442600</td><td id="z{_\" class="">返回支付系统订单号</td></tr><tr id="0ff36d3f-d2be-4f22-a2bb-a4fda270175e"><td id="ubR}" class="">商户号</td><td id="aesD" class="">mchNo</td><td id="abiu" class="">是</td><td id="LpS]" class="">String(30)</td><td id="BL&gt;:" class="">M1621873433953</td><td id="z{_\" class="">商户号</td></tr><tr id="bab8defc-19fc-4c8b-a407-2ef8b3e1b66e"><td id="ubR}" class="">商户订单号</td><td id="aesD" class="">mchOrderNo</td><td id="abiu" class="">是</td><td id="LpS]" class="">String(30)</td><td id="BL&gt;:" class="">20160427210604000490</td><td id="z{_\" class="">返回商户传入的订单号</td></tr><tr id="6c433e89-750c-4fb2-a1bf-5cf30e5f2f00"><td id="ubR}" class="">支付接口</td><td id="aesD" class="">ifCode</td><td id="abiu" class="">是</td><td id="LpS]" class="">String(30)</td><td id="BL&gt;:" class="">wxpay</td><td id="z{_\" class="">支付接口编码</td></tr><tr id="b5d6e62e-bdec-4422-b987-e41e5310d3b9"><td id="ubR}" class="">订单状态</td><td id="aesD" class="">state</td><td id="abiu" class="">是</td><td id="LpS]" class="">int</td><td id="BL&gt;:" class="">2</td><td id="z{_\" class="">支付订单状态<br/>2-<br/><strong>支付成功</strong><br/>5-测试冲正(<br/><strong>已成功且标记为测定订单时返回</strong>)<br/>（2、5视为支付成功，其他均为失败，5只出现在对接测试阶段）<br/></td></tr><tr id="bb298fcc-e8b7-4ad3-9f6b-a4e616c862bb"><td id="ubR}" class="">客户端IP</td><td id="aesD" class="">clientIp</td><td id="abiu" class="">否</td><td id="LpS]" class="">String(32)</td><td id="BL&gt;:" class="">210.73.10.148</td><td id="z{_\" class="">客户端IPV4地址</td></tr><tr id="1a70930e-4f93-4756-82e4-99e0c76a17b7"><td id="ubR}" class="">渠道错误码</td><td id="aesD" class="">errCode</td><td id="abiu" class="">否</td><td id="LpS]" class="">String</td><td id="BL&gt;:" class="">1002</td><td id="z{_\" class="">渠道下单返回错误码</td></tr><tr id="31d9ceb0-e210-4bcb-807d-e6d521472695"><td id="ubR}" class="">渠道错误描述</td><td id="aesD" class="">errMsg</td><td id="abiu" class="">否</td><td id="LpS]" class="">String</td><td id="BL&gt;:" class="">业务异常错误</td><td id="z{_\" class="">渠道下单返回错误描述</td></tr><tr id="453ae789-1678-409b-b37f-5ef251f96bf7"><td id="ubR}" class="">创建时间</td><td id="aesD" class="">createdAt</td><td id="abiu" class="">是</td><td id="LpS]" class="">long</td><td id="BL&gt;:" class="">1622016572190</td><td id="z{_\" class="">订单创建时间,13位时间戳</td></tr><tr id="3eace331-fc9e-4d05-bf9d-e873c68de8c9"><td id="ubR}" class="">成功时间</td><td id="aesD" class="">successTime</td><td id="abiu" class="">否</td><td id="LpS]" class="">long</td><td id="BL&gt;:" class="">1622016572190</td><td id="z{_\" class="">订单支付成功时间,13位时间戳</td></tr></tbody></table><p id="ad275d0f-8d26-4d8e-b552-6cfbb5d8e6bd" class=""><code>返回示例数据</code></p><script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js" integrity="sha512-7Z9J3l1+EYfeaPKcGXu3MS/7T+w19WtKQY/n+xzmw4hZhJ9tyYmcUS+4QqAlzhicE5LAfMQSF3iFTK9bQdTxXg==" crossorigin="anonymous" referrerPolicy="no-referrer"></script><link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism.min.css" integrity="sha512-tN7Ec6zAFaVSG3TpNAKtk4DOHNpSwKHxxrsiw4GHKESGPs5njn/0sMCUMl2svV4wo4BK/rCP7juYz+zx+l6oeQ==" crossorigin="anonymous" referrerPolicy="no-referrer"/><pre id="127b3dec-a322-4059-b07e-3dade4c584d1" class="code"><code class="language-JSON" style="white-space:pre-wrap;word-break:break-all">{
  &quot;code&quot;: 0,
  &quot;data&quot;: {
    &quot;amount&quot;: 58,
    &quot;clientIp&quot;: &quot;192.166.1.132&quot;,
    &quot;createdAt&quot;: 1623985457705,
    &quot;ifCode&quot;: &quot;alipay&quot;,
    &quot;mchNo&quot;: &quot;M1623984572&quot;,
    &quot;mchOrderNo&quot;: &quot;mho1623985457320&quot;,
    &quot;payOrderId&quot;: &quot;P202106181104177050002&quot;,
    &quot;state&quot;: 2,
    &quot;successTime&quot;: 1623985459000,
  },
  &quot;msg&quot;: &quot;SUCCESS&quot;,
  &quot;sign&quot;: &quot;9548145EA12D0CD8C1628BCF44E19E0D&quot;
}</code></pre><h2 id="b54af16a-423b-4a40-8f8c-4a2c99933c15" class=""><strong>支付通知</strong></h2><p id="665a704e-f0f0-465e-960c-c49de09f5db6" class="">当订单支付成功时，支付网关会向商户系统发起回调通知。如果商户系统没有正确返回，支付网关会延迟再次通知。</p><blockquote id="df0bfcbc-fb68-4cab-9c7f-44e7cbea0b1b" class="">接口说明</blockquote><p id="c449a886-fb38-4f8b-82bc-22a628f71c2f" class="">请求URL：该链接是通过统一下单接口提交的参数notifyUrl设置，如果无法访问链接，商户系统将无法接收到支付中心的通知。</p><p id="00c2e77b-f0c0-4b4a-8829-20310c1b00fe" class="">请求方式：<code>POST</code></p><p id="78fb8143-e877-4e3b-a9d0-cf7a4374fbe2" class="">请求类型：<code>application/x-www-form-urlencoded</code></p><blockquote id="5a2dac00-b77c-4df4-9899-2d04723f8273" class="">通知参数</blockquote><table id="8f2620eb-2d6f-4f46-b490-0fdd1d9ec6ea" class="simple-table"><tbody><tr id="d9c8983f-92a9-4668-8694-13767001f2c2"><td id="RQAt" class="">字段名</td><td id="Bi}t" class="" style="width:145px">变量名</td><td id="N^Y&lt;" class="">必填</td><td id="UkkT" class="">类型</td><td id="\RbF" class="">示例值</td><td id="w=h_" class="">描述</td></tr><tr id="8503c93a-e577-4fab-b40f-a280d1070d7e"><td id="RQAt" class="">支付订单号</td><td id="Bi}t" class="" style="width:145px">payOrderId</td><td id="N^Y&lt;" class="">是</td><td id="UkkT" class="">String(30)</td><td id="\RbF" class="">P12021022311124442600</td><td id="w=h_" class="">返回支付系统订单号</td></tr><tr id="2ba0e67b-4430-4d1f-92f8-bd60093268b4"><td id="RQAt" class="">商户号</td><td id="Bi}t" class="" style="width:145px">mchNo</td><td id="N^Y&lt;" class="">是</td><td id="UkkT" class="">String(30)</td><td id="\RbF" class="">M1621873433953</td><td id="w=h_" class="">商户号</td></tr><tr id="ba2ac76d-a905-4391-9fc9-3a7e38903043"><td id="RQAt" class="">商户订单号</td><td id="Bi}t" class="" style="width:145px">mchOrderNo</td><td id="N^Y&lt;" class="">是</td><td id="UkkT" class="">String(30)</td><td id="\RbF" class="">20160427210604000490</td><td id="w=h_" class="">返回商户传入的订单号</td></tr><tr id="a019dd7d-5a58-40ea-885d-28473ab29431"><td id="RQAt" class="">支付接口</td><td id="Bi}t" class="" style="width:145px">ifCode</td><td id="N^Y&lt;" class="">是</td><td id="UkkT" class="">String(30)</td><td id="\RbF" class="">wxpay</td><td id="w=h_" class="">支付接口编码</td></tr><tr id="22cd4d77-ed6a-42fb-a078-d408570ec941"><td id="RQAt" class="">支付金额</td><td id="Bi}t" class="" style="width:145px">amount</td><td id="N^Y&lt;" class="">是</td><td id="UkkT" class="">int</td><td id="\RbF" class="">100</td><td id="w=h_" class="">支付金额,单位分</td></tr><tr id="f9dfd5fb-2c4b-442c-872c-0a19c3783407"><td id="RQAt" class="">订单状态</td><td id="Bi}t" class="" style="width:145px">state</td><td id="N^Y&lt;" class="">是</td><td id="UkkT" class="">int</td><td id="\RbF" class="">2</td><td id="w=h_" class="">支付订单状态<br/>2-<br/><strong>支付成功</strong><br/>5-测试冲正(<br/><strong>已成功且标记为测定订单时返回</strong>)<br/>（2、5视为支付成功，其他均为失败，5只出现在对接测试阶段）<br/></td></tr><tr id="161afea7-6cce-4fd0-bd8f-5cfba844e08b"><td id="RQAt" class="">客户端IP</td><td id="Bi}t" class="" style="width:145px">clientIp</td><td id="N^Y&lt;" class="">否</td><td id="UkkT" class="">String(32)</td><td id="\RbF" class="">210.73.10.148</td><td id="w=h_" class="">客户端IPV4地址</td></tr><tr id="72729bf0-6487-458e-88e8-36062da548da"><td id="RQAt" class="">渠道订单号</td><td id="Bi}t" class="" style="width:145px">channelOrderNo</td><td id="N^Y&lt;" class="">否</td><td id="UkkT" class="">String</td><td id="\RbF" class="">20160427210604000490</td><td id="w=h_" class="">对应渠道的订单号</td></tr><tr id="d8603a00-0c5b-4c3a-bbe8-20c9b3d8af4f"><td id="RQAt" class="">渠道错误码</td><td id="Bi}t" class="" style="width:145px">errCode</td><td id="N^Y&lt;" class="">否</td><td id="UkkT" class="">String</td><td id="\RbF" class="">1002</td><td id="w=h_" class="">渠道下单返回错误码</td></tr><tr id="148aa230-705f-4287-a0c0-b33960e568eb"><td id="RQAt" class="">渠道错误描述</td><td id="Bi}t" class="" style="width:145px">errMsg</td><td id="N^Y&lt;" class="">否</td><td id="UkkT" class="">String</td><td id="\RbF" class="">134586944573118714</td><td id="w=h_" class="">渠道下单返回错误描述</td></tr><tr id="a0b8aecf-7e7f-4e91-af53-578e42485b66"><td id="RQAt" class="">创建时间</td><td id="Bi}t" class="" style="width:145px">createdAt</td><td id="N^Y&lt;" class="">是</td><td id="UkkT" class="">long</td><td id="\RbF" class="">1622016572190</td><td id="w=h_" class="">订单创建时间,13位时间戳</td></tr><tr id="9397499f-c705-4ead-b940-74f9fda6f9e6"><td id="RQAt" class="">成功时间</td><td id="Bi}t" class="" style="width:145px">successTime</td><td id="N^Y&lt;" class="">否</td><td id="UkkT" class="">long</td><td id="\RbF" class="">1622016572190</td><td id="w=h_" class="">订单支付成功时间,13位时间戳</td></tr><tr id="014fa623-ac7d-4926-9499-7cf972b4f7d8"><td id="RQAt" class="">通知请求时间</td><td id="Bi}t" class="" style="width:145px">reqTime</td><td id="N^Y&lt;" class="">是</td><td id="UkkT" class="">String(30)</td><td id="\RbF" class="">1622016572190</td><td id="w=h_" class="">通知请求时间，,13位时间戳</td></tr><tr id="8a60cefc-0e67-475f-a036-68ad2414a447"><td id="RQAt" class="">签名</td><td id="Bi}t" class="" style="width:145px">sign</td><td id="N^Y&lt;" class="">是</td><td id="UkkT" class="">String(32)</td><td id="\RbF" class="">C380BEC2BFD727A4B6845133519F3AD6</td><td id="w=h_" class="">签名值，详见签名算法</td></tr></tbody></table><p id="27267a05-3dd3-4eb5-9252-2e4c3f0e9578" class="">&gt; 返回结果</p><p id="62cb0459-054a-442e-bab2-115ddd7591c1" class="">业务系统处理后同步返回给支付中心，返回字符串 success 则表示成功，返回非success则表示处理失败，支付中心会再次通知业务系统。（通知频率为0/30/60/90/120/150,单位：秒）</p><p id="244b13ae-929d-442b-998d-eb18675a072e" class=""><code>注意：返回的字符串必须是小写，且前后不能有空格和换行符。</code></p><p id="d9b0c26d-a9f3-4864-9f05-c57b5cfa0338" class=""><code>通知示例数据</code></p><script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js" integrity="sha512-7Z9J3l1+EYfeaPKcGXu3MS/7T+w19WtKQY/n+xzmw4hZhJ9tyYmcUS+4QqAlzhicE5LAfMQSF3iFTK9bQdTxXg==" crossorigin="anonymous" referrerPolicy="no-referrer"></script><link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism.min.css" integrity="sha512-tN7Ec6zAFaVSG3TpNAKtk4DOHNpSwKHxxrsiw4GHKESGPs5njn/0sMCUMl2svV4wo4BK/rCP7juYz+zx+l6oeQ==" crossorigin="anonymous" referrerPolicy="no-referrer"/><pre id="9c44b687-545e-40ba-a43c-1892ad8421e4" class="code"><code class="language-JSON" style="white-space:pre-wrap;word-break:break-all">{
    &quot;amount&quot;: 5,
    &quot;clientIp&quot;: &quot;192.166.1.132&quot;,
    &quot;createdAt&quot;: &quot;1622016572190&quot;,
    &quot;ifCode&quot;: &quot;wxpay&quot;,
    &quot;mchNo&quot;: &quot;M1621873433953&quot;,
    &quot;mchOrderNo&quot;: &quot;mho1621934803068&quot;,
    &quot;payOrderId&quot;: &quot;20210525172643357010&quot;,
    &quot;state&quot;: 3,
    &quot;sign&quot;: &quot;C380BEC2BFD727A4B6845133519F3AD6&quot;
}</code></pre><h2 id="f54f4576-0d17-4607-9d66-ed6b4c88e21e" class=""><strong>返回码</strong></h2><table id="314181f3-e1cf-4dc4-973e-71f9fec4d20e" class="simple-table"><tbody><tr id="13c0f63b-ab65-4e5b-b792-c6d932a42112"><td id="lSr}" class="">code</td><td id="@i?}" class="">描述</td></tr><tr id="406afd03-0dde-4ae9-bebf-ed2e498ee62d"><td id="lSr}" class="">0</td><td id="@i?}" class="">成功</td></tr><tr id="670d1707-b219-49d2-9c09-d21a3046465e"><td id="lSr}" class="">9999</td><td id="@i?}" class="">异常，具体错误详见msg字段</td></tr></tbody></table></div></article><span class="sans" style="font-size:14px;padding-top:2em"></span></body></html>