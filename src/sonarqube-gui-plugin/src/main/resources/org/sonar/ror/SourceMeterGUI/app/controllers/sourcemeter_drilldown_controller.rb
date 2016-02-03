# Copyright (c) 2014-2016, FrontEndART Software Ltd.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# 1. Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright
#    notice, this list of conditions and the following disclaimer in the
#    documentation and/or other materials provided with the distribution.
# 3. All advertising materials mentioning features or use of this software
#    must display the following acknowledgement:
#    This product includes software developed by FrontEndART Software Ltd.
# 4. Neither the name of FrontEndART Software Ltd. nor the
#    names of its contributors may be used to endorse or promote products
#    derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY FrontEndART Software Ltd. ''AS IS'' AND ANY
# EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL FrontEndART Software Ltd. BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

class SourcemeterDrilldownController < ApplicationController
     
  def load_drilldown
    @sourcemeter_qualifiers = java_facade.getComponentByClassname('SourceMeterGUI', 'com.sourcemeter.gui.resources.SourceMeterQualifiers').class
    @project=Project.by_key(params[:data_key])
    @snapshot=Snapshot.find(:last, :conditions => ['project_id=?', @project.id], :order => 'created_at desc')
    @resource=@snapshot.resource
  end
  
  def load_method_drilldown
    @sourcemeter_qualifiers = java_facade.getComponentByClassname('SourceMeterGUI', 'com.sourcemeter.gui.resources.SourceMeterQualifiers').class
    if (params[:snapshot_id])
      @snapshot=Snapshot.find(:last, :conditions => ['id=?', params[:snapshot_id]], :order => 'created_at desc')
      @resource=@snapshot.resource
    else
      @project = Project.find(:last, :conditions => ['id=?', params[:project_id]], :order => 'created_at desc')
      @snapshot = @project.last_snapshot
      @resource = @snapshot.resource
    end
  end

  def load_metrics
    require 'json'
    @metric=Metric.by_key(params[:metric])
    @sourcemeter_qualifiers = java_facade.getComponentByClassname('SourceMeterGUI', 'com.sourcemeter.gui.resources.SourceMeterQualifiers').class

    if (params[:snapshot_id])
      @snapshot=Snapshot.find(:last, :conditions => ['id=?', params[:snapshot_id]], :order => 'created_at desc')
      @resource=@snapshot.resource
    else
      @project = Project.find(:last, :conditions => ['id=?', params[:project_id]], :order => 'created_at desc')
      @snapshot = @project.last_snapshot
      @resource = @snapshot.resource
    end
  end

  def filepaths
    render :partial => 'filepaths'
  end
  
  def is_sm_resource_in_file
    project=Project.by_key(URI.unescape(params[:data_key]))
    snapshot=Snapshot.find(:last, :conditions => ['project_id=?', project.id], :order => 'created_at desc')
    sm_resource = snapshot.measure('SM:resource')
    if sm_resource == nil
      render :text => 'false'
    else
      render :text => 'true'
    end
  end

  def ca_metrics_by_list(metrics_list, snapshot, zero_if_missing, display_key, hasHelp, method_qualifiers, class_qualifiers)
    table = ''
    metrics_list.each do |metric_obj|
      if(!metric_obj.nil?)
        measure_obj = snapshot.measure(metric_obj.key);
        mvalue=nil;
        if(!measure_obj.nil? && metric_obj.numeric? && !measure_obj.value.nil?)
          mvalue = measure_obj.value;
        elsif(zero_if_missing == true)
          mvalue = 0;
        end
                  
        threshold=java_facade.getConfigurationValue('sm.' + @resource.language + '.' + @resource.description + '.baseline.' + metric_obj.key)
        if(threshold.nil?)
          baseline_def=Api::Utils.java_facade.propertyDefinitions.get('sm.' + @resource.language + '.' + @resource.description + '.baseline.' + metric_obj.key)
          if(baseline_def.nil?)
            if(@resource.qualifier == @sourcemeter_qualifiers::BASE_CLASS_QUALIFIER)
              threshold=java_facade.getConfigurationValue('sm.' + @resource.language + '.class.baseline.' + metric_obj.key)
              if(threshold.nil?)
                baseline_def=Api::Utils.java_facade.propertyDefinitions.get('sm.' + @resource.language + '.class.baseline.' + metric_obj.key)
                if(!baseline_def.nil?)
                  threshold=baseline_def.defaultValue
                end
              end
            elsif(@resource.qualifier == @sourcemeter_qualifiers::BASE_METHOD_QUALIFIER || @resource.qualifier == @sourcemeter_qualifiers::BASE_FUNCTION_QUALIFIER)
              threshold=java_facade.getConfigurationValue('sm.' + @resource.language + '.method.baseline.' + metric_obj.key)
              if(threshold.nil?)
                baseline_def=Api::Utils.java_facade.propertyDefinitions.get('sm.' + @resource.language + '.method.baseline.' + metric_obj.key)
                if(!baseline_def.nil?)
                  threshold=baseline_def.defaultValue
                end
              end
            end
          else
            threshold=baseline_def.defaultValue
          end
        end
        if(!measure_obj.nil? || zero_if_missing)
          if(!threshold.blank?)
            if(metric_obj.direction <= 0)
              table += '<tr class="baseline_bad"><td class="icons">' + @template.image_tag('/images/test/ERROR.png', {:height => '12px'}) + '</td>' if mvalue > threshold.to_s.gsub(',', '.').to_f
              table += '<tr class="baseline_good"><td class="icons">' + @template.image_tag('/images/test/OK.png', {:height => '12px'}) + '</td>' if mvalue <= threshold.to_s.gsub(',', '.').to_f
            else
              table += '<tr class="baseline_bad"><td class="icons">' + @template.image_tag('/images/test/ERROR.png', {:height => '12px'}) + '</td>' if mvalue < threshold.to_s.gsub(',', '.').to_f
              table += '<tr class="baseline_good"><td class="icons">' + @template.image_tag('/images/test/OK.png', {:height => '12px'}) + '</td>' if mvalue >= threshold.to_s.gsub(',', '.').to_f
            end
          else
            table += '<tr><td></td>'
          end
          metric_name = metric_obj.short_name
          threshold_s = ''
          case metric_obj.val_type
          when Metric::VALUE_TYPE_INT
            threshold_s = @template.number_with_precision(threshold.to_i, :precision => 0)
          when Metric::VALUE_TYPE_FLOAT
            threshold_s = @template.number_with_precision(threshold.to_s.gsub(',', '.').to_f, :precision => 1)
          when Metric::VALUE_TYPE_PERCENT
            threshold_s = @template.number_to_percentage(threshold.to_s.gsub(',', '.').to_f, {:precision => 1})
          else
            threshold_s = threshold
          end
          if display_key && !threshold.blank?
            metric_name += ' (' + metric_obj.key + ', ' + threshold_s + ')'
          elsif display_key && threshold.blank?
            metric_name += ' (' + metric_obj.key + ')'
          elsif !display_key && !threshold.blank?
            metric_name += ' (' + threshold_s + ')'
          end
          table += '<td class="name">'
          if hasHelp
            table += @template.link_to(metric_name, {:controller => 'plugins', :action => 'home', :page => 'sm-help', :language => @resource.language, :metric => metric_obj.key}, { :class => 'metric_ref', :popup => ['help', 'height=900,width=1250,scrollbars=1,resizable=1']})
          else
            table += metric_name
          end
          table += ':</td>'
          table += '<td class="value">'+ measure_obj.formatted_value + '</td>' if !measure_obj.nil?
          table += '<td class="value">'+ mvalue.to_s + '</td>' if measure_obj.nil?
          table += '</tr>'
        end
      end
    end
    table
  end
  helper_method :ca_metrics_by_list

  def ca_metrics_by_domain(title, domain, snapshot, zero_if_missing, display_key, hasHelp, method_qualifiers, class_qualifiers)
    metrics_of_domain = Metric.by_domain(domain)
    ca_metrics = ca_metrics_by_list(metrics_of_domain, snapshot, zero_if_missing, display_key, hasHelp, method_qualifiers, class_qualifiers)
    table = ""
    if !ca_metrics.empty?
      table = '<table class="metrics">'
      table += '<thead><tr><th colspan="3">' + title + '</th></tr></thead>'
      table += '<tbody>'
      table += ca_metrics
      table += '</tbody></table>'
    end
  end
  helper_method :ca_metrics_by_domain

  # metrictables: [{title: 'title', domain: domain: 'domain', hide_key: true/false}]
  def metrics_header_column_by_domain(metrictables, zero_if_missing, method_qualifiers, class_qualifiers)
    table  = '<table><tbody>'
    metrictables.each do |mtable|
      display_key = true unless mtable[:hide_key]
      help = true unless (mtable[:hasHelp] == false)
      ca_metrics = ca_metrics_by_domain(mtable[:title], mtable[:domain], @snapshot, zero_if_missing, display_key, help, method_qualifiers, class_qualifiers)
      if !ca_metrics.nil? && !ca_metrics.empty?
        table += '<tr><td class="col">'
        table += ca_metrics
        table += '</td></tr>'
      end
    end
    table += '</tbody></table>'
  end
  helper_method :metrics_header_column_by_domain

  # metrictables: [{title: 'title', metrics: [m1, m2, ...]}, hide_key: true/false, hasHelp: true/false]
  def metrics_header_column_by_list(metrictables, zero_if_missing, method_qualifiers, class_qualifiers)
    table  = '<table><tbody>'
    metrictables.each do |mtable|
      display_key = true unless mtable[:hide_key]
      help = true unless (mtable[:hasHelp] == false)
        ca_metrics = ca_metrics_by_list(mtable[:metrics], @snapshot, zero_if_missing, display_key, help, method_qualifiers, class_qualifiers)
      if !ca_metrics.nil? && !ca_metrics.empty?
        table += '<tr><td class="col">'
        table += '<table class="metrics">'
        table += '<thead><tr><th colspan="3">' + mtable[:title] + '</th></tr></thead>'
        table += '<tbody>'
        table += ca_metrics
        table += '</tbody></table>'
        table += '</td></tr>'
      end
    end
    table += '</tbody></table>'
  end
  helper_method :metrics_header_column_by_list

  def draw_maintainability()
    table = '<table class="metrics commercial"><tr>'
    url = @template.url_for_static(:plugin => 'SourceMeterGUI', :path => 'SourceMeterLogo.png')
    table += '<th style="padding-top: 8px">' + @template.image_tag("#{url}", {:height => '12px'})
    table += '&nbsp;Powered by <a href="https://www.frontendart.com" target="FrontEndART">FrontEndART</a> '
    table += '<a href="https://sourcemeter.com" target="SourceMeter">SourceMeter</a></th>'
    table += '</tr></table>'
  end
  helper_method :draw_maintainability
end