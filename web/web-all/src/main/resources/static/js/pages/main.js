$(function () {

  'use strict';

  var $distpicker = $('#distpicker');

  $distpicker.distpicker({
    province:'Fujian Province',
    city:'Xiamen City',
    district:'Siming District'
  });

  $('#reset').click(function () {
    $distpicker.distpicker('reset');
  });

  $('#reset-deep').click(function () {
    $distpicker.distpicker('reset', true);
  });

  $('#destroy').click(function () {
    $distpicker.distpicker('destroy');
  });

  $('#distpicker1').distpicker();

  $('#distpicker2').distpicker({
    province:'---- Province where ----',
    city:'---- City ----',
    district:'---- the district ----'
  });

  $('#distpicker3').distpicker({
    province:'Zhejiang Province',
    city:'Hangzhou City',
    district:'West Lake District'
  });

  $('#distpicker4').distpicker({
    placeholder: false
  });

  $('#distpicker5').distpicker({
    autoSelect: false
  });

});